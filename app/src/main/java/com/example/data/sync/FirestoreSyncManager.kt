package com.example.data.sync

import android.util.Log
import com.example.data.db.AppDatabase
import com.example.data.model.Expense
import com.example.data.model.PoolDeposit
import com.example.data.model.RoomEvent
import com.example.data.model.Roommate
import com.example.data.repository.ExpenseRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * Manages two-way real-time synchronization between the Android Room Database
 * and Firebase Cloud Firestore (document: roommate_groups/group_124).
 * This enables instant updates across both the Netlify Web Application and the Android APK.
 */
class FirestoreSyncManager(
    private val repository: ExpenseRepository,
    private val database: AppDatabase,
    private val coroutineScope: CoroutineScope
) {
    private var firestore: FirebaseFirestore? = null
    private var listenerRegistration: ListenerRegistration? = null
    private val syncDocPath = "roommate_groups/group_124"

    private var onStatusChanged: ((String) -> Unit)? = null
    private var isSyncingFromRemote = false

    fun init(statusCallback: (String) -> Unit) {
        onStatusChanged = statusCallback
        try {
            val db = FirebaseFirestore.getInstance()
            firestore = db
            onStatusChanged?.invoke("🟢 Connecting to Cloud Sync...")
            startListening()
        } catch (e: Exception) {
            Log.w("FirestoreSyncManager", "Firebase not yet initialized. Provide google-services.json to sync: ${e.message}")
            onStatusChanged?.invoke("🟢 Local Storage • Add google-services.json for Web Sync")
        }
    }

    private fun startListening() {
        val db = firestore ?: return
        try {
            val docRef = db.document(syncDocPath)
            listenerRegistration = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("FirestoreSyncManager", "Listen error: ${error.message}")
                    onStatusChanged?.invoke("🟡 Cloud Sync Reconnecting...")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    onStatusChanged?.invoke("🟢 Live Real-Time Web Sync Active")
                    val data = snapshot.data ?: return@addSnapshotListener

                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            isSyncingFromRemote = true
                            processRemoteSnapshot(data)
                        } catch (e: Exception) {
                            Log.e("FirestoreSyncManager", "Error processing remote snapshot", e)
                        } finally {
                            isSyncingFromRemote = false
                        }
                    }
                } else {
                    onStatusChanged?.invoke("🟢 Cloud Room Connected (Ready)")
                }
            }
        } catch (e: Exception) {
            Log.e("FirestoreSyncManager", "Error attaching listener", e)
            onStatusChanged?.invoke("🟡 Cloud Sync Ready")
        }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun processRemoteSnapshot(data: Map<String, Any>) {
        // Sync remote transactions if present
        val remoteTxList = data["transactions"] as? List<Map<String, Any>>
        if (remoteTxList != null) {
            val existingExpenses = repository.allExpenses.firstOrNull() ?: emptyList()
            val existingTitles = existingExpenses.map { "${it.title}_${it.amount}_${it.paidByMemberName}" }.toSet()

            for (tx in remoteTxList) {
                val title = tx["title"] as? String ?: continue
                val amount = (tx["amount"] as? Number)?.toDouble() ?: continue
                val paidByName = tx["paidByName"] as? String ?: "Roommate"
                val key = "${title}_${amount}_${paidByName}"

                if (!existingTitles.contains(key)) {
                    val category = tx["category"] as? String ?: "Groceries"
                    val isRoomExpense = (tx["isRoomExpense"] as? Boolean) ?: true
                    val notes = tx["notes"] as? String ?: tx["note"] as? String ?: "Synced from Web"
                    val approvalStatus = tx["approvalStatus"] as? String ?: "APPROVED"
                    val isSettled = (tx["isSettled"] as? Boolean) ?: false
                    val timestamp = (tx["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()

                    val newExpense = Expense(
                        title = title,
                        amount = amount,
                        paidByMemberId = 1,
                        paidByMemberName = paidByName,
                        category = category,
                        isRoomExpense = isRoomExpense,
                        paymentSource = "POOL",
                        timestamp = timestamp,
                        notes = notes,
                        isSettled = isSettled,
                        approvalStatus = approvalStatus
                    )
                    repository.insertExpense(newExpense)
                }
            }
        }
    }

    /**
     * Push a newly created local expense to the shared Firestore cloud document
     * so it instantly reflects on the Netlify web dashboard.
     */
    fun pushExpense(expense: Expense) {
        val db = firestore ?: return
        if (isSyncingFromRemote) return

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val allExp = repository.allExpenses.firstOrNull() ?: emptyList()
                val txMapList = allExp.map { e ->
                    mapOf(
                        "id" to "tx-${e.id}",
                        "title" to e.title,
                        "amount" to e.amount,
                        "category" to e.category,
                        "isRoomExpense" to e.isRoomExpense,
                        "paidById" to "${e.paidByMemberId}",
                        "paidByName" to e.paidByMemberName,
                        "paymentSource" to e.paymentSource,
                        "timestamp" to e.timestamp,
                        "note" to e.notes,
                        "notes" to e.notes,
                        "isSettled" to e.isSettled,
                        "approvalStatus" to e.approvalStatus
                    )
                }

                val totalDep = repository.totalDeposits.firstOrNull() ?: 20000.0
                val totalDeb = repository.totalPoolDebits.firstOrNull() ?: 0.0

                val payload = mapOf(
                    "transactions" to txMapList,
                    "totalGathered" to totalDep,
                    "remainingPoolBalance" to (totalDep - totalDeb),
                    "lastUpdated" to System.currentTimeMillis()
                )

                db.document(syncDocPath).set(payload, SetOptions.merge())
            } catch (e: Exception) {
                Log.w("FirestoreSyncManager", "Failed to push expense to cloud: ${e.message}")
            }
        }
    }

    /**
     * Push a new deposit contribution to the shared Firestore cloud document.
     */
    fun pushDeposit(deposit: PoolDeposit) {
        val db = firestore ?: return
        if (isSyncingFromRemote) return

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val allDeposits = repository.allDeposits.firstOrNull() ?: emptyList()
                val depMapList = allDeposits.map { d ->
                    mapOf(
                        "id" to "dep-${d.id}",
                        "memberId" to "${d.memberId}",
                        "memberName" to d.memberName,
                        "amount" to d.amount,
                        "timestamp" to d.timestamp,
                        "note" to d.note
                    )
                }
                val totalDep = repository.totalDeposits.firstOrNull() ?: 20000.0

                val payload = mapOf(
                    "deposits" to depMapList,
                    "totalGathered" to totalDep,
                    "lastUpdated" to System.currentTimeMillis()
                )

                db.document(syncDocPath).set(payload, SetOptions.merge())
            } catch (e: Exception) {
                Log.w("FirestoreSyncManager", "Failed to push deposit to cloud: ${e.message}")
            }
        }
    }

    fun stop() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }
}
