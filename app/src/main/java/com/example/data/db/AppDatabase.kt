package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Expense
import com.example.data.model.PoolDeposit
import com.example.data.model.Roommate
import com.example.data.model.RoomEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Roommate::class, PoolDeposit::class, Expense::class, RoomEvent::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun roommateDao(): RoommateDao
    abstract fun poolDepositDao(): PoolDepositDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun roomEventDao(): RoomEventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "roommate_expenses_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Populate default sample data in a coroutine
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(database: AppDatabase) {
                val roommateDao = database.roommateDao()
                val depositDao = database.poolDepositDao()
                val expenseDao = database.expenseDao()
                val roomEventDao = database.roomEventDao()

                // Insert roommates with 1 or more Admins (Vinod Kumar as primary admin, Alex Morgan as co-admin)
                val roommates = listOf(
                    Roommate(
                        id = 1,
                        name = "Vinod Kumar",
                        email = "uvinodkumar614@gmail.com",
                        pin = "1234",
                        avatarColorHex = "#4F46E5",
                        role = "Admin",
                        phoneOrNote = "+91 98765 43210",
                        isAdmin = true,
                        autoPayEnabled = true,
                        autoPayAmount = 500.0,
                        autoPayDay = 1,
                        autoPaySource = "Google Pay UPI (vinod@okaxis)",
                        linkedAccounts = "HDFC Bank (••••4321)|Google Pay (vinod@okaxis)"
                    ),
                    Roommate(
                        id = 2,
                        name = "Alex Morgan",
                        email = "alex.morgan@roommail.com",
                        pin = "1234",
                        avatarColorHex = "#10B981",
                        role = "Admin",
                        phoneOrNote = "+91 98765 43211",
                        isAdmin = true,
                        autoPayEnabled = true,
                        autoPayAmount = 500.0,
                        autoPayDay = 1,
                        autoPaySource = "PhonePe UPI (alex@ybl)",
                        linkedAccounts = "Chase Bank (••••8819)|PhonePe (alex@ybl)"
                    ),
                    Roommate(
                        id = 3,
                        name = "David Chen",
                        email = "david.chen@roommail.com",
                        pin = "1234",
                        avatarColorHex = "#F59E0B",
                        role = "Roommate",
                        phoneOrNote = "+91 98765 43212",
                        isAdmin = false,
                        autoPayEnabled = false,
                        autoPayAmount = 500.0,
                        autoPayDay = 1,
                        autoPaySource = "Paytm UPI",
                        linkedAccounts = "Bank of America (••••1024)"
                    ),
                    Roommate(
                        id = 4,
                        name = "Sam Wilson",
                        email = "sam.wilson@roommail.com",
                        pin = "1234",
                        avatarColorHex = "#EC4899",
                        role = "Roommate",
                        phoneOrNote = "+91 98765 43213",
                        isAdmin = false,
                        autoPayEnabled = false,
                        autoPayAmount = 500.0,
                        autoPayDay = 1,
                        autoPaySource = "Google Pay UPI",
                        linkedAccounts = "Wells Fargo (••••9942)"
                    )
                )
                roommateDao.insertRoommates(roommates)

                val now = System.currentTimeMillis()
                val day = 24 * 60 * 60 * 1000L

                // Each member contributed ₹5,000 to form the initial ₹20,000 shared wallet pool
                val deposits = listOf(
                    PoolDeposit(id = 1, memberId = 1, memberName = "Vinod Kumar", amount = 5000.0, timestamp = now - 15 * day, note = "Pool deposit by Vinod (₹5,000)"),
                    PoolDeposit(id = 2, memberId = 2, memberName = "Alex Morgan", amount = 5000.0, timestamp = now - 15 * day, note = "Pool deposit by Alex (₹5,000)"),
                    PoolDeposit(id = 3, memberId = 3, memberName = "David Chen", amount = 5000.0, timestamp = now - 15 * day, note = "Pool deposit by David (₹5,000)"),
                    PoolDeposit(id = 4, memberId = 4, memberName = "Sam Wilson", amount = 5000.0, timestamp = now - 15 * day, note = "Pool deposit by Sam (₹5,000)")
                )
                depositDao.insertDeposits(deposits)

                // Add initial sample room expenses & personal spends from pool
                val sampleExpenses = listOf(
                    Expense(
                        id = 1,
                        title = "Monthly Room Rent",
                        amount = 12000.0,
                        paidByMemberId = 1,
                        paidByMemberName = "Vinod Kumar",
                        category = "Room Rent",
                        isRoomExpense = true,
                        paymentSource = "POOL",
                        timestamp = now - 12 * day,
                        notes = "Paid room rent directly from pooled ₹20,000",
                        isSettled = false,
                        approvalStatus = "ACTIVE"
                    ),
                    Expense(
                        id = 2,
                        title = "Weekly Grocery Store Purchase",
                        amount = 150.0,
                        paidByMemberId = 2,
                        paidByMemberName = "Alex Morgan",
                        category = "Groceries",
                        isRoomExpense = true,
                        paymentSource = "POOL",
                        timestamp = now - 8 * day,
                        notes = "Milk, vegetables, bread, shared snacks debited from pool",
                        isSettled = false,
                        approvalStatus = "ACTIVE"
                    ),
                    Expense(
                        id = 3,
                        title = "Personal Food Delivery & Snacks",
                        amount = 45.0,
                        paidByMemberId = 3,
                        paidByMemberName = "David Chen",
                        category = "Personal",
                        isRoomExpense = false,
                        paymentSource = "POOL",
                        timestamp = now - 4 * day,
                        notes = "Used pool wallet for personal food order - Can be settled or approved",
                        isSettled = false,
                        approvalStatus = "PERSONAL_PENDING"
                    ),
                    Expense(
                        id = 4,
                        title = "High-Speed WiFi Broadband Bill",
                        amount = 60.0,
                        paidByMemberId = 4,
                        paidByMemberName = "Sam Wilson",
                        category = "Wifi",
                        isRoomExpense = true,
                        paymentSource = "POOL",
                        timestamp = now - 3 * day,
                        notes = "Monthly shared fiber internet",
                        isSettled = false,
                        approvalStatus = "ACTIVE"
                    ),
                    Expense(
                        id = 5,
                        title = "Drinking Water Can Refills",
                        amount = 35.0,
                        paidByMemberId = 1,
                        paidByMemberName = "Vinod Kumar",
                        category = "Water",
                        isRoomExpense = true,
                        paymentSource = "POOL",
                        timestamp = now - 1 * day,
                        notes = "10 drinking water cans for room",
                        isSettled = false,
                        approvalStatus = "ACTIVE"
                    )
                )
                expenseDao.insertExpenses(sampleExpenses)

                // Initialize The Event Side: Chores, Cooking rotations & Utility bill countdowns
                val sampleEvents = listOf(
                    // Cleaning Duties
                    RoomEvent(
                        id = 1,
                        title = "Kitchen Counter & Stove Deep Clean",
                        category = "CLEANING",
                        assignedMemberId = 2,
                        assignedMemberName = "Alex Morgan",
                        assignedMemberColorHex = "#10B981",
                        dueDate = now + 1 * day,
                        dayOfWeek = 1, // Mon
                        status = "IN_PROGRESS",
                        isCompleted = false,
                        recurrence = "WEEKLY",
                        notes = "Wipe stove, degrease hood, disinfect counters and clean sink drain"
                    ),
                    RoomEvent(
                        id = 2,
                        title = "Living Room Mop & Carpet Vacuum",
                        category = "CLEANING",
                        assignedMemberId = 3,
                        assignedMemberName = "David Chen",
                        assignedMemberColorHex = "#F59E0B",
                        dueDate = now + 2 * day,
                        dayOfWeek = 2, // Tue
                        status = "TODO",
                        isCompleted = false,
                        recurrence = "WEEKLY",
                        notes = "Vacuum sofa, sweep under TV stand, mop wood floors"
                    ),
                    RoomEvent(
                        id = 3,
                        title = "Bathroom Scrubbing & Mirror Polish",
                        category = "CLEANING",
                        assignedMemberId = 4,
                        assignedMemberName = "Sam Wilson",
                        assignedMemberColorHex = "#EC4899",
                        dueDate = now + 3 * day,
                        dayOfWeek = 3, // Wed
                        status = "TODO",
                        isCompleted = false,
                        recurrence = "WEEKLY",
                        notes = "Clean shower tiles, toilet bowl, and mirror glass"
                    ),
                    RoomEvent(
                        id = 4,
                        title = "Balcony Sweep & Trash Bins Curbside",
                        category = "CLEANING",
                        assignedMemberId = 1,
                        assignedMemberName = "Vinod Kumar",
                        assignedMemberColorHex = "#4F46E5",
                        dueDate = now,
                        dayOfWeek = 7, // Sun
                        status = "COMPLETED",
                        isCompleted = true,
                        completedAt = now - 2 * 60 * 60 * 1000L,
                        completedByMemberName = "Vinod Kumar",
                        recurrence = "WEEKLY",
                        notes = "Take dry & wet bins to collection area downstairs"
                    ),

                    // Cooking Rotations
                    RoomEvent(
                        id = 5,
                        title = "Monday Dinner: Pasta & Italian Sauce",
                        category = "COOKING",
                        assignedMemberId = 1,
                        assignedMemberName = "Vinod Kumar",
                        assignedMemberColorHex = "#4F46E5",
                        dueDate = now + 1 * day,
                        dayOfWeek = 1,
                        status = "TODO",
                        isCompleted = false,
                        recurrence = "WEEKLY",
                        notes = "Garlic bread + Penne Arrabiata for 4 roommates"
                    ),
                    RoomEvent(
                        id = 6,
                        title = "Tuesday Dinner: Curry & Fragrant Rice Feast",
                        category = "COOKING",
                        assignedMemberId = 2,
                        assignedMemberName = "Alex Morgan",
                        assignedMemberColorHex = "#10B981",
                        dueDate = now + 2 * day,
                        dayOfWeek = 2,
                        status = "TODO",
                        isCompleted = false,
                        recurrence = "WEEKLY",
                        notes = "Paneer butter masala / Chicken curry with basmati rice"
                    ),
                    RoomEvent(
                        id = 7,
                        title = "Wednesday Dinner: Healthy Stir Fry & Noodles",
                        category = "COOKING",
                        assignedMemberId = 3,
                        assignedMemberName = "David Chen",
                        assignedMemberColorHex = "#F59E0B",
                        dueDate = now + 3 * day,
                        dayOfWeek = 3,
                        status = "TODO",
                        isCompleted = false,
                        recurrence = "WEEKLY",
                        notes = "Fresh veggies, tofu/chicken, soy sauce and noodles"
                    ),
                    RoomEvent(
                        id = 8,
                        title = "Weekend Special: Rooftop Barbecue / Feast",
                        category = "COOKING",
                        assignedMemberId = 4,
                        assignedMemberName = "Sam Wilson",
                        assignedMemberColorHex = "#EC4899",
                        dueDate = now + 6 * day,
                        dayOfWeek = 6,
                        status = "TODO",
                        isCompleted = false,
                        recurrence = "WEEKLY",
                        notes = "Grilled skewers, burgers, cold drinks"
                    ),

                    // Utility Bill Countdowns
                    RoomEvent(
                        id = 9,
                        title = "Electricity Board Power Bill",
                        category = "UTILITY_BILL",
                        assignedMemberId = 1,
                        assignedMemberName = "Vinod Kumar",
                        assignedMemberColorHex = "#4F46E5",
                        dueDate = now + 3 * day,
                        dayOfWeek = 3,
                        status = "TODO",
                        isCompleted = false,
                        isBill = true,
                        billAmount = 85.0,
                        recurrence = "MONTHLY",
                        notes = "Account #EB-99214. Consumer portal link active. Pay before penalty date!"
                    ),
                    RoomEvent(
                        id = 10,
                        title = "High-Speed WiFi Fiber Internet Recharge",
                        category = "UTILITY_BILL",
                        assignedMemberId = 4,
                        assignedMemberName = "Sam Wilson",
                        assignedMemberColorHex = "#EC4899",
                        dueDate = now + 5 * day,
                        dayOfWeek = 5,
                        status = "TODO",
                        isCompleted = false,
                        isBill = true,
                        billAmount = 60.0,
                        recurrence = "MONTHLY",
                        notes = "300 Mbps unlimited fiber connection renewal"
                    ),
                    RoomEvent(
                        id = 11,
                        title = "Monthly Room Apartment Rent Payment",
                        category = "UTILITY_BILL",
                        assignedMemberId = 1,
                        assignedMemberName = "Vinod Kumar",
                        assignedMemberColorHex = "#4F46E5",
                        dueDate = now + 6 * day,
                        dayOfWeek = 6,
                        status = "TODO",
                        isCompleted = false,
                        isBill = true,
                        billAmount = 1200.0,
                        recurrence = "MONTHLY",
                        notes = "Transfer directly to landlord via NEFT/UPI pool funds"
                    ),
                    RoomEvent(
                        id = 12,
                        title = "LPG Cooking Gas Cylinder Refill",
                        category = "UTILITY_BILL",
                        assignedMemberId = 2,
                        assignedMemberName = "Alex Morgan",
                        assignedMemberColorHex = "#10B981",
                        dueDate = now + 12 * day,
                        dayOfWeek = 5,
                        status = "TODO",
                        isCompleted = false,
                        isBill = true,
                        billAmount = 32.0,
                        recurrence = "MONTHLY",
                        notes = "Book online via Indane / HP gas portal"
                    ),
                    RoomEvent(
                        id = 13,
                        title = "Drinking Water 20L Dispenser Recharge",
                        category = "UTILITY_BILL",
                        assignedMemberId = 3,
                        assignedMemberName = "David Chen",
                        assignedMemberColorHex = "#F59E0B",
                        dueDate = now + 2 * day,
                        dayOfWeek = 2,
                        status = "TODO",
                        isCompleted = false,
                        isBill = true,
                        billAmount = 25.0,
                        recurrence = "MONTHLY",
                        notes = "5 cans prepaid token booklet"
                    )
                )
                roomEventDao.insertEvents(sampleEvents)
            }
        }
    }
}
