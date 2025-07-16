package com.madtitan.estimator

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.madtitan.estimator.core.domain.Category
import com.madtitan.estimator.core.domain.CategoryWithSubCategories
import com.madtitan.estimator.core.domain.Payment
import com.madtitan.estimator.core.domain.SubCategory
import com.madtitan.estimator.feature_auth.ui.LoginScreen
import com.madtitan.estimator.feature_auth.viewmodel.AllTransactionsViewModel
import com.madtitan.estimator.feature_auth.viewmodel.BudgetViewModel
import com.madtitan.estimator.feature_auth.viewmodel.CategoryViewModel
import com.madtitan.estimator.feature_auth.viewmodel.PaymentViewModel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed class Screen(val route: String)  {
    //open val route: String get() = this::class.simpleName!!.lowercase()
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Dashboard : Screen("dashboard")
    data object AddExpense : Screen("add_expense")
    data object AllTransactions : Screen("all_transactions")
    data object CategoryManagement : Screen("category_management")

    data object PaymentDetail : Screen("payment_detail/{paymentId}") {
        fun createRoute(paymentId: String): String = "payment_detail/$paymentId"
    }
}

data class FilterState(
    val modes: Set<String> = emptySet(),
    val startDate: Timestamp? = null,
    val endDate: Timestamp? = null,
    val transactionType: String? = null,  // expense/income/borrow/lent
    val tag: String? = null,
    val linkedToTag: String? = null,
    val categoryId: String? = null,
    val subCategoryId: String? = null
)

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController, startDestination = Screen.Splash.route, modifier = modifier) {
        composable(Screen.Splash.route) {
            SplashRouter(navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController)
        }
        composable(Screen.AddExpense.route) {
            AddExpenseScreen(navController)
        }
        composable(Screen.AllTransactions.route) {
            AllTransactionsScreen(navController)
        }
        composable(Screen.CategoryManagement.route) {
            CategoryManagementScreen(navController)
        }
        composable(
            route = Screen.PaymentDetail.createRoute("{paymentId}"),
            arguments = listOf(navArgument("paymentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val paymentId = backStackEntry.arguments?.getString("paymentId") ?: return@composable
            PaymentDetailScreen(paymentId = paymentId)
        }


    }
}

@Composable
fun SplashRouter(navController: NavHostController) {
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            initializeTagCountersIfNeeded()
            // User is signed in, go to dashboard
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        } else {
            // User not signed in, go to login
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    // Optional splash/loading UI
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun DashboardScreen(navController: NavHostController,
                    viewModel: BudgetViewModel = hiltViewModel()) {
    val recentTransactions by viewModel.fetchRecentPayments().collectAsState(initial = emptyList())
    val user = FirebaseAuth.getInstance().currentUser

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Welcome, ${user?.displayName ?: "User"} 👋",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ➕ Add Expense button
        Button(
            onClick = { navController.navigate(Screen.AddExpense.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Expense")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                navController.navigate(Screen.CategoryManagement.route)
            }
        ) {
            Icon(Icons.Outlined.List, contentDescription = "Manage Categories")
            Spacer(Modifier.width(8.dp))
            Text("Manage Categories")
        }

        Spacer(modifier = Modifier.height(24.dp))

        RecentTransactionsSection(
            recentTransactions = recentTransactions,
            onPaymentClick = { paymentId ->
                navController.navigate(Screen.PaymentDetail.createRoute(paymentId))
            },
            onViewAllClick = {
                navController.navigate(Screen.AllTransactions.route)
            }
        )

    }
}


@Composable
fun AddExpenseScreen(navController: NavHostController, viewModel: BudgetViewModel = hiltViewModel(),
                     categoryViewModel: CategoryViewModel = hiltViewModel()) {
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var paymentMode by remember { mutableStateOf("Online") }
    val paymentModes = listOf("Online", "Cash", "Card")
    // ✅ Load category list once
    val categories = categoryViewModel.categoryList
    var subcategories by remember { mutableStateOf<List<SubCategory>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedSubCategory by remember { mutableStateOf<SubCategory?>(null) }
    // For dialog state
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddSubCategoryDialog by remember { mutableStateOf(false) }

    val types = listOf("income", "expense", "borrow", "lent")
    var type by remember { mutableStateOf("expense") }
    var counterpartyName by remember { mutableStateOf("") }

    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Holds the timestamp the user chose (defaults to now)
    var chosenTimestamp by remember { mutableStateOf(Timestamp.now()) }
    // Formatter for display
    val displayFormatter = remember {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    }
    LaunchedEffect(selectedCategory) {
        subcategories = selectedCategory?.let {
            categoryViewModel.categoryList
                .firstOrNull { it.category.id == selectedCategory!!.id }
                ?.subCategories ?: emptyList()
        } ?: emptyList()
        selectedSubCategory = null // Reset subcategory when category changes
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text("Add Expense", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth()
        )

        // Simplified Dropdown

        var expanded by remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = paymentMode,
                onValueChange = {},
                label = { Text("Payment Mode") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                paymentModes.forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            paymentMode = it
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TypeDropdown(
            types = types,
            selectedType = type,
            onTypeSelected = { type = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (type == "borrow" || type == "lent") {
            OutlinedTextField(
                value = counterpartyName,
                onValueChange = { counterpartyName = it },
                label = { Text("Person Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }


        CategoryDropdown(
            categories = categories.map { it.category },
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it },
            onAddNewCategory = { showAddCategoryDialog = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedCategory != null) {
            SubCategoryDropdown(
                subcategories = subcategories,
                selectedSubCategory = selectedSubCategory,
                onSubCategorySelected = { selectedSubCategory = it },
                onAddNewSubCategory = { showAddSubCategoryDialog = true }
            )
        }

        // DateTime display & picker launcher
        Button(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(displayFormatter.format(chosenTimestamp.toDate()))
        }
        Spacer(Modifier.height(24.dp))


        Button(
            onClick = {
                val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
                val payment = Payment(
                    id = UUID.randomUUID().toString(),
                    amount = amount.toDoubleOrNull() ?: 0.0,
                    notes = notes,
                    paymentMode = paymentMode,
                    payeeId = "", // you can associate later
                    accountId = userId ,     // if you track events
                    timestamp = chosenTimestamp,
                    category = selectedCategory?.id ?: "",
                    subCategory = selectedSubCategory?.id ?: "",
                    type = type,
                    counterpartyName = if (type == "borrow" || type == "lent") counterpartyName else ""
                )
                viewModel.addPayment(payment) {
                    navController.popBackStack() // navigate back
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Expense")
        }

        // Date picker
        // Show DatePickerDialog when requested
        if (showDatePicker) {
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    showDatePicker = false
                    showTimePicker = true
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Show TimePickerDialog after date is picked
        if (showTimePicker) {
            android.app.TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, 0)

                    chosenTimestamp = Timestamp(calendar.time)
                    showTimePicker = false
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ).show()
        }

        if (showAddCategoryDialog) {
            SimpleInputDialog(
                title = "Add Category",
                onDismiss = { showAddCategoryDialog = false },
                onConfirm = { name ->
                    categoryViewModel.addCategory(name)
                    showAddCategoryDialog = false
                }
            )
        }

        if (showAddSubCategoryDialog && selectedCategory != null) {
            SimpleInputDialog(
                title = "Add Subcategory",
                onDismiss = { showAddSubCategoryDialog = false },
                onConfirm = { name ->
                    categoryViewModel.addSubCategory(name, selectedCategory!!.id)
                    showAddSubCategoryDialog = false
                }
            )
        }

    }
}

@Composable
fun RecentTransactionsSection(
    recentTransactions: List<Payment>,
    onPaymentClick: (String) -> Unit,
    onViewAllClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.clickable { onViewAllClick() }
            )
            Text(
                text = "View All",
                style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary),
                modifier = Modifier.clickable { onViewAllClick() }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(recentTransactions) { payment ->
                PaymentListItem(payment = payment) {
                    onPaymentClick(payment.id)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTransactionsScreen(navController: NavController) {
    val payments = remember { mutableStateListOf<Payment>() }

    // TODO: Load from ViewModel
    LaunchedEffect(Unit) {
        // collect and update payments list
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("All Transactions") })
        }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            items(payments) { payment ->
                PaymentListItem(payment = payment) {
                    navController.navigate(Screen.PaymentDetail.createRoute(payment.id  ))
                }
            }
        }
    }
}

@Composable
fun PaymentDetailScreen(paymentId: String) {
    val viewModel: PaymentViewModel = hiltViewModel()
    val categoryViewModel: CategoryViewModel = hiltViewModel()
    val payment by viewModel.getPaymentById(paymentId).collectAsState(initial = null)
    /*
    //category name display
    val categoryName = categoryViewModel.categoryList
        .firstOrNull { it.category.id == payment.categoryId }
        ?.category?.name.orEmpty()

    val subCategoryName = categoryViewModel.categoryList
        .firstOrNull { it.category.id == payment.categoryId }
        ?.subCategories?.firstOrNull { it.id == payment.subCategoryId }
        ?.name.orEmpty()*/

    payment?.let {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Type: ${it.type.replaceFirstChar { char -> char.uppercaseChar() }}")
            Text("Payment Mode: ${it.paymentMode}")
            Text("Amount: ₹${it.amount}")
            Text("Notes: ${it.notes}")
            Text("Timestamp: ${it.timestamp.toDate()}")
            Text("Category: ${categoryViewModel.categoryList
                .firstOrNull { cat-> cat.category.id == it.category }
                ?.category?.name.orEmpty()}")
            Text("Sub Category: ${categoryViewModel.categoryList
                .firstOrNull { cat-> cat.category.id == it.category }
                ?.subCategories?.firstOrNull { subCat -> subCat.id == it.subCategory }
                ?.name.orEmpty()}")

            if (it.type == "borrow" || it.type == "lent") {
                Text("Person: ${it.counterpartyName}")
            }


        }
    } ?: run {
        Text("Loading payment...")
    }
}

@Composable
fun PaymentListItem(
    payment: Payment,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = payment.paymentMode.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "₹%.2f".format(payment.amount),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            // 👇 Insert this line
            Text(
                text = buildString {
                    append(payment.type.uppercase())
                    if (payment.type == "borrow" || payment.type == "lent") {
                        append(" • ${payment.counterpartyName}")
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (payment.notes.isNotBlank()) {
                Text(
                    text = payment.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Text(
                text = payment.timestamp.toDate().formatAsDisplayDate(),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private fun Date.formatAsDisplayDate(): String {
    val format = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return format.format(this)
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AllTransactionsScreen(
    navController: NavHostController,
    viewModel: AllTransactionsViewModel = hiltViewModel()
) {
    val payments by viewModel.payments.collectAsState()
    val grouped by viewModel.groupedPayments.collectAsState()
    val filters by viewModel.filterState.collectAsState()
    val listState = rememberLazyListState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var sheetVisible by remember { mutableStateOf(false) }

    // Trigger loadNextPage when reaching the bottom
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .filterNotNull()
            .filter { it >= payments.size - 1 }
            .collect { viewModel.loadNextPage() }
    }


    // ✅ Sheet only appears when visible
    if (sheetVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    sheetState.hide()
                    sheetVisible = false
                }
            },
            sheetState = sheetState
        ) {
            FilterContent(
                current = filters,
                onApply = { modes, start, end ->
                    viewModel.updateModes(modes)
                    viewModel.updateDateRange(start, end)
                    scope.launch {
                        sheetState.hide()
                        sheetVisible = false
                    }
                },
                onClear = {
                    viewModel.clearFilter()
                }
            )
        }
    }

    Scaffold(
        topBar = {
            AllTransactionsTopBar {
                // launch only in response to the button click
                sheetVisible = true
                scope.launch { sheetState.show() }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            // Active filter chips
            FilterChips(filters, onRemoveMode = {
                viewModel.updateModes(filters.modes - it)
            }, onRemoveDate = {
                viewModel.updateDateRange(null, null)
            })

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                grouped.forEach { (dateLabel, itemsForDay) ->
                    val dayTotal = itemsForDay.sumOf { it.amount }

                    stickyHeader {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dateLabel,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "₹%.2f".format(dayTotal),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    items(items = itemsForDay ) { payment ->
                        PaymentListItem(payment = payment) {
                            navController.navigate(Screen.PaymentDetail.createRoute(payment.id))
                        }
                    }
                }

                if (grouped.isEmpty()) {
                    item {
                        Text(
                            "No transactions found.",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }

    }

    // ✅ Handle back press
    BackHandler (enabled = true) { if (sheetVisible) {
        scope.launch {
            sheetState.hide()
            sheetVisible = false
        }
    } else {
        navController.popBackStack()
    } }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTransactionsTopBar(
    onFilterClicked: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = { Text("All Transactions") },
        actions = {
            // Make sure to name the parameter and call launch inside the onClick lambda:
            IconButton(onClick = onFilterClicked) {
                Icon(Icons.Default.Settings, contentDescription = "Filter")
            }
        }
    )
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterChips(
    filters: FilterState,
    onRemoveMode: (String) -> Unit,
    onRemoveDate: () -> Unit
) {
    val chips = remember {
        mutableListOf<@Composable () -> Unit>()
    }.apply {
        clear()
        filters.modes.forEach { mode ->
            add {
                AssistChip(
                    onClick = { onRemoveMode(mode) },
                    label = { Text(mode) },
                    leadingIcon = { Icon(Icons.Default.Check, null) }
                )
            }
        }
        if (filters.startDate != null && filters.endDate != null) {
            val fmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val label = "${fmt.format(filters.startDate.toDate())}→${fmt.format(filters.endDate.toDate())}"
            add {
                AssistChip(
                    onClick = onRemoveDate,
                    label = { Text(label) },
                    trailingIcon = { Icon(Icons.Default.Close, null) }
                )
            }
        }
        if (filters.modes.isNotEmpty() || filters.startDate != null) {
            add {
                AssistChip(
                    onClick = onRemoveDate, // clear all
                    label = { Text("Clear Filters") }
                )
            }
        }
    }

    if (chips.isNotEmpty()) {
        FlowRow(
            modifier = Modifier.padding(8.dp),
            // replace mainAxisSpacing = 8.dp
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            // replace crossAxisSpacing = 8.dp
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            chips.forEach { it() }
        }
    }
}

@Composable
fun FilterContent(
    current: FilterState,
    onApply: (modes: Set<String>, start: Timestamp?, end: Timestamp?) -> Unit,
    onClear: () -> Unit
) {
    val modes = listOf("Cash", "Online", "Card")
    val selectedModes = remember { mutableStateListOf<String>().apply { addAll(current.modes) } }

    // Date pickers:
    val context = LocalContext.current
    val cal = remember { Calendar.getInstance() }
    var pickStart by remember { mutableStateOf(current.startDate) }
    var pickEnd by remember { mutableStateOf(current.endDate) }
    var pickStartDate by remember { mutableStateOf(false) }
    var pickEndDate by remember { mutableStateOf(false) }

    Column(Modifier.padding(16.dp)) {
        Text("Filter Transactions", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(16.dp))
        Text("Payment Modes")
        modes.forEach { mode ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = selectedModes.contains(mode),
                    onCheckedChange = {
                        if (it) selectedModes.add(mode) else selectedModes.remove(mode)
                    }
                )
                Text(mode, modifier = Modifier.clickable {
                    if (selectedModes.contains(mode)) selectedModes.remove(mode) else selectedModes.add(mode)
                })
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Date Range")
        Row {
            OutlinedButton(onClick = { pickStartDate = true }) {
                Text(pickStart?.toDate()?.let {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                } ?: "Start")
            }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = { pickEndDate = true }) {
                Text(pickEnd?.toDate()?.let {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                } ?: "End")
            }
        }

        Spacer(Modifier.height(24.dp))
        Row {
            Button(onClick = { onApply(selectedModes.toSet(), pickStart, pickEnd) }) {
                Text("Apply")
            }
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = onClear) {
                Text("Clear All")
            }
        }
    }

    // Start date picker
    if (pickStartDate) {
        DatePickerDialog(
            context,
            { _, y, m, d ->
                cal.set(y, m, d)
                pickStart = Timestamp(cal.time)
                pickStartDate = false
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    // End date picker
    if (pickEndDate) {
        DatePickerDialog(
            context,
            { _, y, m, d ->
                cal.set(y, m, d)
                pickEnd = Timestamp(cal.time)
                pickEndDate = false
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}

fun initializeTagCountersIfNeeded() {
    val firestore = FirebaseFirestore.getInstance()
    val tagCounterDoc = firestore.collection("metadata").document("tag_counters")

    tagCounterDoc.get().addOnSuccessListener { doc ->
        if (!doc.exists()) {
            tagCounterDoc.set(
                mapOf(
                    "incomeCounter" to 0,
                    "expenseCounter" to 0,
                    "borrowCounter" to 0,
                    "lentCounter" to 0
                )
            )
        }
    }
}


@Composable
fun CategoryManagementScreen(navController: NavHostController,viewModel: CategoryViewModel = hiltViewModel()) {
    val categories = viewModel.categoryList
    val expandedIds = viewModel.expandedIds.value

    // Dialog state
    var showCategoryDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }

    var showSubCategoryDialog by remember { mutableStateOf(false) }
    var subCategoryToEdit by remember { mutableStateOf<SubCategory?>(null) }
    var subCategoryParent: Category? by remember { mutableStateOf(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                categoryToEdit = null
                showCategoryDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Manage Categories", style = MaterialTheme.typography.titleLarge)

            Spacer(Modifier.height(16.dp))

            LazyColumn {
                items(categories) { item ->
                    CategoryItem(
                        item = item,
                        isExpanded = expandedIds.contains(item.category.id),
                        onToggleExpand = { viewModel.toggleExpanded(item.category.id) },
                        onEdit = {
                            categoryToEdit = item.category
                            showCategoryDialog = true
                        },
                        onDelete = { viewModel.deleteCategory( it ) },
                        onAddSubCategory = {
                            subCategoryParent = it
                            subCategoryToEdit = null
                            showSubCategoryDialog = true
                        },
                        onEditSubCategory = {
                            subCategoryToEdit = it
                            subCategoryParent = null
                            showSubCategoryDialog = true
                        },
                        onDeleteSubCategory = { viewModel.deleteSubCategory(it) }
                    )
                }
            }
        }
    }

    if (showCategoryDialog) {
        CategoryDialog(
            initialName = categoryToEdit?.name.orEmpty(),
            title = if (categoryToEdit == null) "Add Category" else "Edit Category",
            onDismiss = { showCategoryDialog = false },
            onConfirm = { name ->
                if (categoryToEdit == null) {
                    viewModel.addCategory(name)
                } else {
                    viewModel.updateCategory(categoryToEdit!!.copy(name = name))
                }
                showCategoryDialog = false
            }
        )
    }

    if (showSubCategoryDialog) {
        SubCategoryDialog(
            initialName = subCategoryToEdit?.name.orEmpty(),
            title = if (subCategoryToEdit == null) "Add Subcategory" else "Edit Subcategory",
            onDismiss = { showSubCategoryDialog = false },
            onConfirm = { name ->
                if (subCategoryToEdit == null) {
                    viewModel.addSubCategory(name, subCategoryParent?.id ?: "")
                } else {
                    viewModel.updateSubCategory(subCategoryToEdit!!.copy(name = name))
                }
                showSubCategoryDialog = false
            }
        )
    }
}


@Composable
fun CategoryDialog(
    initialName: String,
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onConfirm(name.trim()) }) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Category Name") },
                singleLine = true
            )
        }
    )
}

@Composable
fun SubCategoryDialog(
    initialName: String,
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onConfirm(name.trim()) }) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Subcategory Name") },
                singleLine = true
            )
        }
    )
}

@Composable
fun CategoryItem(
    item: CategoryWithSubCategories,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onEdit: () -> Unit,
    onDelete: (Category) -> Unit, // ✅ accepts category
    onAddSubCategory: (Category) -> Unit, // ✅ accepts category
    onEditSubCategory: (SubCategory) -> Unit,
    onDeleteSubCategory: (SubCategory) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = item.category.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onToggleExpand) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Category")
            }

            IconButton(onClick = { onDelete(item.category) }) { // ✅ use item
                Icon(Icons.Default.Delete, contentDescription = "Delete Category")
            }
        }

        if (isExpanded) {
            Spacer(Modifier.height(8.dp))
            item.subCategories.forEach { sub ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
                ) {
                    Text(
                        text = sub.name,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    IconButton(onClick = { onEditSubCategory(sub) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Subcategory")
                    }
                    IconButton(onClick = { onDeleteSubCategory(sub) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Subcategory")
                    }
                }
            }

            OutlinedButton(
                onClick = { onAddSubCategory(item.category) }, // ✅ use item
                modifier = Modifier.padding(top = 8.dp, start = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Add Subcategory")
            }
        }
    }
}

@Composable
fun SimpleInputDialog(
    title: String,
    initialText: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (text.isNotBlank()) onConfirm(text.trim())
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CategoryDropdown(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category?) -> Unit,
    onAddNewCategory: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedCategory?.name ?: "",
            onValueChange = {},
            label = { Text("Category") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }

            HorizontalDivider()

            DropdownMenuItem(
                text = { Text("+ Add New Category") },
                onClick = {
                    onAddNewCategory()
                    expanded = false
                }
            )
        }
    }
}

@Composable
fun SubCategoryDropdown(
    subcategories: List<SubCategory>,
    selectedSubCategory: SubCategory?,
    onSubCategorySelected: (SubCategory?) -> Unit,
    onAddNewSubCategory: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedSubCategory?.name ?: "",
            onValueChange = {},
            label = { Text("Subcategory") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            subcategories.forEach { sub ->
                DropdownMenuItem(
                    text = { Text(sub.name) },
                    onClick = {
                        onSubCategorySelected(sub)
                        expanded = false
                    }
                )
            }

            HorizontalDivider()

            DropdownMenuItem(
                text = { Text("+ Add New Subcategory") },
                onClick = {
                    onAddNewSubCategory()
                    expanded = false
                }
            )
        }
    }
}


@Composable
fun TypeDropdown(
    types: List<String>,
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedType,
            onValueChange = {},
            label = { Text("Transaction Type") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            types.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.replaceFirstChar { it.uppercaseChar() }) },
                    onClick = {
                        onTypeSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}





