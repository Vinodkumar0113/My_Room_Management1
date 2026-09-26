package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Roommate
import com.example.ui.theme.EmeraldPositive
import com.example.ui.theme.RoseDebit
import com.example.ui.viewmodel.UiState

data class PresetMerchant(
    val name: String,
    val upiId: String,
    val defaultCategory: String,
    val suggestedAmount: Double,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanPayScreen(
    uiState: UiState,
    onPayViaQr: (
        merchantOrPayee: String,
        amount: Double,
        paidByMemberId: Long,
        paidByMemberName: String,
        category: String,
        isRoomExpense: Boolean,
        notes: String,
        upiOrQrData: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var isFlashOn by remember { mutableStateOf(false) }
    var manualQrInput by remember { mutableStateOf("") }
    var selectedPayeeName by remember { mutableStateOf<String?>(null) }
    var selectedUpiId by remember { mutableStateOf<String?>(null) }
    var initialAmount by remember { mutableStateOf<Double?>(null) }
    var initialCategory by remember { mutableStateOf("Groceries") }

    var showPaymentSheet by remember { mutableStateOf(false) }
    var paymentSuccessData by remember { mutableStateOf<Pair<String, Double>?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Preset scan QR codes for quick real-life room utility and store payments
    val presetMerchants = listOf(
        PresetMerchant("City Fresh Supermarket", "citysupermarket@okhdfcbank", "Groceries", 65.50, Icons.Default.ShoppingCart, Color(0xFF10B981)),
        PresetMerchant("State Electricity Board", "statepowerbill@icici", "Electricity", 48.00, Icons.Default.Bolt, Color(0xFFF59E0B)),
        PresetMerchant("Metro Drinking Water", "metrowater@paytm", "Water", 25.00, Icons.Default.WaterDrop, Color(0xFF0284C7)),
        PresetMerchant("Fiber WiFi Broadband", "airtelbroadband@axis", "Wifi", 55.00, Icons.Default.Wifi, Color(0xFF8B5CF6)),
        PresetMerchant("Building Landlord Rent", "greenapartments@sbi", "Room Rent", 1200.00, Icons.Default.Home, Color(0xFF4F46E5)),
        PresetMerchant("Corner Cafe & Bakery", "cornercafe@ybl", "Food", 18.25, Icons.Default.Coffee, Color(0xFFEC4899))
    )

    // Infinite animation for scanner laser beam
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val scanProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_laser_progress"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header & Available Wallet Balance
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Direct QR Scanner",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Scan any UPI or store QR to pay from Room Wallet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                        modifier = Modifier.testTag("wallet_balance_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Wallet",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$${String.format("%.2f", uiState.remainingPoolBalance)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Camera Scanner Viewfinder Box
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .testTag("camera_viewfinder_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Flash & Gallery Controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.6f)
                        ) {
                            IconButton(
                                onClick = { isFlashOn = !isFlashOn },
                                modifier = Modifier.size(40.dp).testTag("flash_toggle_button")
                            ) {
                                Icon(
                                    imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                    contentDescription = "Flashlight",
                                    tint = if (isFlashOn) Color(0xFFFBBF24) else Color.White
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.6f)
                        ) {
                            IconButton(
                                onClick = {
                                    // Trigger default sample merchant scan
                                    val demo = presetMerchants.first()
                                    selectedPayeeName = demo.name
                                    selectedUpiId = demo.upiId
                                    initialCategory = demo.defaultCategory
                                    initialAmount = demo.suggestedAmount
                                    showPaymentSheet = true
                                },
                                modifier = Modifier.size(40.dp).testTag("gallery_picker_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Upload QR from Gallery",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    // Target Viewfinder Reticle Frame
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .align(Alignment.Center)
                            .border(
                                width = 2.dp,
                                color = if (isFlashOn) Color(0xFF60A5FA) else Color(0xFF38BDF8).copy(alpha = 0.8f),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        // Animated Laser Scan Line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .offset(y = (scanProgress * 180).dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color(0xFF38BDF8),
                                            Color(0xFF818CF8),
                                            Color(0xFF38BDF8),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        // Center QR Icon watermark
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scanner Target",
                            tint = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier
                                .size(64.dp)
                                .align(Alignment.Center)
                        )
                    }

                    // Scanning Instruction Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.7f),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = "Point camera at any QR code to pay",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Preset Scan Targets (Instant Mock / Test Payee QRs)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Instant Tap & Pay QRs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Tap any merchant QR below to simulate an instant scan",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(presetMerchants) { merchant ->
                        Card(
                            onClick = {
                                selectedPayeeName = merchant.name
                                selectedUpiId = merchant.upiId
                                initialCategory = merchant.defaultCategory
                                initialAmount = merchant.suggestedAmount
                                showPaymentSheet = true
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.width(160.dp).testTag("preset_qr_${merchant.defaultCategory}")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(merchant.color.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = merchant.icon,
                                        contentDescription = merchant.name,
                                        tint = merchant.color,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = merchant.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )

                                Text(
                                    text = "$${String.format("%.2f", merchant.suggestedAmount)}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldPositive
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = merchant.color.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "Scan QR",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = merchant.color,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Paste Custom UPI ID or QR Text
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Or Enter UPI ID / QR String Manually",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = manualQrInput,
                            onValueChange = { manualQrInput = it },
                            placeholder = { Text("e.g. storename@upi") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("manual_upi_input")
                        )

                        Button(
                            onClick = {
                                if (manualQrInput.isNotBlank()) {
                                    selectedPayeeName = manualQrInput.substringBefore("@").replace(".", " ").capitalize()
                                    selectedUpiId = manualQrInput
                                    initialAmount = 50.0
                                    initialCategory = "Groceries"
                                    showPaymentSheet = true
                                }
                            },
                            enabled = manualQrInput.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("submit_manual_upi_button")
                        ) {
                            Text("Pay")
                        }
                    }
                }
            }
        }
    }

    // Payment Confirmation Bottom Sheet
    if (showPaymentSheet && selectedPayeeName != null) {
        DirectQrPaymentBottomSheet(
            payeeName = selectedPayeeName!!,
            upiId = selectedUpiId ?: "upi@merchant",
            initialCategory = initialCategory,
            initialAmount = initialAmount ?: 0.0,
            remainingWalletBalance = uiState.remainingPoolBalance,
            roommates = uiState.roommates,
            currentMember = uiState.currentMember,
            isAdmin = uiState.isAdminLoggedIn,
            sheetState = sheetState,
            onDismiss = { showPaymentSheet = false },
            onConfirmPayment = { merchant, amt, memberId, memberName, cat, isRoom, notes, upi ->
                onPayViaQr(merchant, amt, memberId, memberName, cat, isRoom, notes, upi)
                showPaymentSheet = false
                paymentSuccessData = Pair(merchant, amt)
            }
        )
    }

    // Payment Success Dialog
    paymentSuccessData?.let { (merchant, amount) ->
        AlertDialog(
            onDismissRequest = { paymentSuccessData = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(EmeraldPositive.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = EmeraldPositive,
                        modifier = Modifier.size(36.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Payment Successful!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$${String.format("%.2f", amount)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldPositive
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Paid to $merchant",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "✓ Debited from Shared Room Wallet",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { paymentSuccessData = null },
                    modifier = Modifier.fillMaxWidth().testTag("payment_success_done_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Done")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectQrPaymentBottomSheet(
    payeeName: String,
    upiId: String,
    initialCategory: String,
    initialAmount: Double,
    remainingWalletBalance: Double,
    roommates: List<Roommate>,
    currentMember: Roommate?,
    isAdmin: Boolean,
    sheetState: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit,
    onConfirmPayment: (
        merchant: String,
        amount: Double,
        memberId: Long,
        memberName: String,
        category: String,
        isRoomExpense: Boolean,
        notes: String,
        upiId: String
    ) -> Unit
) {
    var amountText by remember { mutableStateOf(if (initialAmount > 0) String.format("%.2f", initialAmount) else "") }
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var isRoomExpense by remember { mutableStateOf(true) }
    var selectedMember by remember { mutableStateOf(currentMember ?: roommates.firstOrNull()) }
    var notes by remember { mutableStateOf("Paid via QR Scan") }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var memberDropdownExpanded by remember { mutableStateOf(false) }

    val categories = listOf("Groceries", "Room Rent", "Electricity", "Water", "Wifi", "Maintenance", "Food", "Personal", "Other")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan Pay",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pay via QR Code",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Payee Info Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Merchant",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = payeeName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = upiId,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldPositive.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Verified QR",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldPositive,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Amount Input
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount to Pay ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("qr_payment_amount_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Expense Type: Room Shared vs Personal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = isRoomExpense,
                    onClick = {
                        isRoomExpense = true
                        if (selectedCategory == "Personal") selectedCategory = "Groceries"
                    },
                    label = { Text("🏠 Room Shared") }
                )
                FilterChip(
                    selected = !isRoomExpense,
                    onClick = {
                        isRoomExpense = false
                        selectedCategory = "Personal"
                    },
                    label = { Text("👤 Personal Spend") }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category Picker
            if (isRoomExpense) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { categoryDropdownExpanded = true }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Category: $selectedCategory", fontWeight = FontWeight.Medium)
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select")
                    }

                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Paying Roommate Picker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { memberDropdownExpanded = true }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Authorized By: ${selectedMember?.name ?: "Select Roommate"}",
                        fontWeight = FontWeight.Medium
                    )
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select")
                }

                DropdownMenu(
                    expanded = memberDropdownExpanded,
                    onDismissRequest = { memberDropdownExpanded = false }
                ) {
                    roommates.forEach { mem ->
                        DropdownMenuItem(
                            text = { Text(mem.name + if (mem.isAdmin) " 👑 (Admin)" else "") },
                            onClick = {
                                selectedMember = mem
                                memberDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val amount = amountText.toDoubleOrNull() ?: 0.0
            val isBalanceSufficient = amount <= remainingWalletBalance

            if (!isBalanceSufficient && amount > 0) {
                Text(
                    text = "⚠️ Amount exceeds current wallet balance ($${String.format("%.2f", remainingWalletBalance)})",
                    style = MaterialTheme.typography.bodySmall,
                    color = RoseDebit,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    val member = selectedMember ?: return@Button
                    val finalCategory = if (!isRoomExpense) "Personal" else selectedCategory
                    onConfirmPayment(
                        payeeName,
                        amount,
                        member.id,
                        member.name,
                        finalCategory,
                        isRoomExpense,
                        notes,
                        upiId
                    )
                },
                enabled = amount > 0 && selectedMember != null,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPositive, contentColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirm_qr_payment_button")
            ) {
                Icon(imageVector = Icons.Default.Payment, contentDescription = "Pay", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pay $${String.format("%.2f", amount)} from Room Wallet",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
