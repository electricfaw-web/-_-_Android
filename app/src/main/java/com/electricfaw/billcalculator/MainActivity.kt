package com.electricfaw.billcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

// الألوان
private val ColorBg = Color(0xFFFFF6E0)
private val ColorSurface = Color(0xFFFFFFFF)
private val ColorSurface2 = Color(0xFFFFF0C7)
private val ColorText = Color(0xFF26324A)
private val ColorTextSoft = Color(0xFF5B6B8C)
private val ColorAccent = Color(0xFFE8A317)
private val ColorAccentDark = Color(0xFFB67A0E)
private val ColorNavy = Color(0xFF1B2A4A)
private val ColorBorder = Color(0xFFF0DBA0)
private val ColorDanger = Color(0xFFC0392B)

// الألوان الليلية
private val NightBg = Color(0xFF0B1224)
private val NightSurface = Color(0xFF16213A)
private val NightSurface2 = Color(0xFF1C2A4A)
private val NightText = Color(0xFFEAF2FF)
private val NightTextSoft = Color(0xFF9FB3D9)
private val NightBorder = Color(0xFF26385F)
private val NightAccent = Color(0xFFF5B93A)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BillCalculatorApp()
        }
    }
}

@Composable
fun BillCalculatorApp() {
    var isDarkTheme by remember { mutableStateOf(false) }

    val bgColor = if (isDarkTheme) NightBg else ColorBg
    val surfaceColor = if (isDarkTheme) NightSurface else ColorSurface
    val textColor = if (isDarkTheme) NightText else ColorText
    val softTextColor = if (isDarkTheme) NightTextSoft else ColorTextSoft
    val accentColor = if (isDarkTheme) NightAccent else ColorAccent
    val accentDarkColor = if (isDarkTheme) NightAccent else ColorAccentDark

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = bgColor
    ) {
        BillScreen(
            isDarkTheme = isDarkTheme,
            onThemeToggle = { isDarkTheme = !isDarkTheme },
            bgColor = bgColor,
            surfaceColor = surfaceColor,
            textColor = textColor,
            softTextColor = softTextColor,
            accentColor = accentColor,
            accentDarkColor = accentDarkColor
        )
    }
}

@Composable
fun BillScreen(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    bgColor: Color,
    surfaceColor: Color,
    textColor: Color,
    softTextColor: Color,
    accentColor: Color,
    accentDarkColor: Color
) {
    var selectedCategory by remember { mutableStateOf("منزلي") }
    var selectedMeterType by remember { mutableStateOf("عادي") }
    var previousReading by remember { mutableStateOf("") }
    var currentReading by remember { mutableStateOf("") }
    var ctFactor by remember { mutableStateOf("") }
    var previousDate by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }
    var meterDigits by remember { mutableStateOf("5") }
    var errorMessage by remember { mutableStateOf("") }
    var billResult by remember { mutableStateOf<BillResult?>(null) }

    val ctEligibleCategories = listOf("تجاري", "زراعي", "صناعي", "حكومي")
    val showMeterTypeCard = ctEligibleCategories.contains(selectedCategory)
    val showCTField = showMeterTypeCard && selectedMeterType == "C.T"
    val showRolloverWarning = try {
        previousReading.toDouble() > currentReading.toDouble() && previousReading.isNotEmpty() && currentReading.isNotEmpty()
    } catch (e: Exception) {
        false
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(accentColor, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "احسب فاتورتك",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textColor
                        )
                        Text(
                            "حاسبة فاتورة الكهرباء",
                            fontSize = 11.sp,
                            color = softTextColor
                        )
                    }
                }

                IconButton(onClick = onThemeToggle) {
                    Icon(
                        if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "تبديل الثيم",
                        tint = accentDarkColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // نوع الاشتراك
        item {
            CardComponent(
                surfaceColor = surfaceColor,
                softTextColor = softTextColor,
                accentDarkColor = accentDarkColor
            ) {
                SectionTitle(
                    icon = Icons.Default.Business,
                    title = "اختر نوع الاشتراك",
                    softTextColor = softTextColor,
                    accentDarkColor = accentDarkColor
                )

                // الصف الأول
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryButton(
                        text = "منزلي",
                        icon = Icons.Default.Home,
                        isSelected = selectedCategory == "منزلي",
                        accentColor = accentColor,
                        accentDarkColor = accentDarkColor,
                        softTextColor = softTextColor,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedCategory = "منزلي"
                            selectedMeterType = "عادي"
                        }
                    )
                    CategoryButton(
                        text = "تجاري",
                        icon = Icons.Default.Store,
                        isSelected = selectedCategory == "تجاري",
                        accentColor = accentColor,
                        accentDarkColor = accentDarkColor,
                        softTextColor = softTextColor,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedCategory = "تجاري"
                            selectedMeterType = "عادي"
                        }
                    )
                    CategoryButton(
                        text = "زراعي",
                        icon = Icons.Default.Spa,
                        isSelected = selectedCategory == "زراعي",
                        accentColor = accentColor,
                        accentDarkColor = accentDarkColor,
                        softTextColor = softTextColor,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedCategory = "زراعي"
                            selectedMeterType = "عادي"
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // الصف الثاني
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryButton(
                        text = "صناعي",
                        icon = Icons.Default.Factory,
                        isSelected = selectedCategory == "صناعي",
                        accentColor = accentColor,
                        accentDarkColor = accentDarkColor,
                        softTextColor = softTextColor,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedCategory = "صناعي"
                            selectedMeterType = "عادي"
                        }
                    )
                    CategoryButton(
                        text = "حكومي",
                        icon = Icons.Default.AccountBalance,
                        isSelected = selectedCategory == "حكومي",
                        accentColor = accentColor,
                        accentDarkColor = accentDarkColor,
                        softTextColor = softTextColor,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedCategory = "حكومي"
                            selectedMeterType = "عادي"
                        }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // نوع المقياس (C.T)
        if (showMeterTypeCard) {
            item {
                CardComponent(
                    surfaceColor = surfaceColor,
                    softTextColor = softTextColor,
                    accentDarkColor = accentDarkColor
                ) {
                    SectionTitle(
                        icon = Icons.Default.Speed,
                        title = "نوع المقياس",
                        softTextColor = softTextColor,
                        accentDarkColor = accentDarkColor
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CategoryButton(
                            text = "عادي",
                            icon = Icons.Default.Speed,
                            isSelected = selectedMeterType == "عادي",
                            accentColor = accentColor,
                            accentDarkColor = accentDarkColor,
                            softTextColor = softTextColor,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedMeterType = "عادي" }
                        )
                        CategoryButton(
                            text = "C.T",
                            icon = Icons.Default.Settings,
                            isSelected = selectedMeterType == "C.T",
                            accentColor = accentColor,
                            accentDarkColor = accentDarkColor,
                            softTextColor = softTextColor,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedMeterType = "C.T" }
                        )
                    }

                    if (showCTField) {
                        Spacer(modifier = Modifier.height(12.dp))
                        InputField(
                            label = "معامل الضرب المثبت على المقياس (نسبة C.T)",
                            value = ctFactor,
                            onValueChange = { ctFactor = it },
                            placeholder = "مثال: 40",
                            softTextColor = softTextColor,
                            accentColor = accentColor,
                            textColor = textColor
                        )
                    }
                }
            }
        }

        // قراءة العداد
        item {
            CardComponent(
                surfaceColor = surfaceColor,
                softTextColor = softTextColor,
                accentDarkColor = accentDarkColor
            ) {
                SectionTitle(
                    icon = Icons.Default.AvTimer,
                    title = "قراءة العداد",
                    softTextColor = softTextColor,
                    accentDarkColor = accentDarkColor
                )

                // القراءات
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    InputField(
                        label = "القراءة السابقة",
                        value = previousReading,
                        onValueChange = { previousReading = it },
                        placeholder = "مثال: 1500",
                        softTextColor = softTextColor,
                        accentColor = accentColor,
                        textColor = textColor,
                        modifier = Modifier.weight(1f)
                    )
                    InputField(
                        label = "القراءة الحالية",
                        value = currentReading,
                        onValueChange = { currentReading = it },
                        placeholder = "مثال: 1800",
                        softTextColor = softTextColor,
                        accentColor = accentColor,
                        textColor = textColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // التواريخ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    InputField(
                        label = "تاريخ القراءة السابقة",
                        value = previousDate,
                        onValueChange = { previousDate = it },
                        placeholder = "YYYY-MM-DD",
                        softTextColor = softTextColor,
                        accentColor = accentColor,
                        textColor = textColor,
                        modifier = Modifier.weight(1f)
                    )
                    InputField(
                        label = "تاريخ القراءة الحالية",
                        value = currentDate,
                        onValueChange = { currentDate = it },
                        placeholder = "YYYY-MM-DD",
                        softTextColor = softTextColor,
                        accentColor = accentColor,
                        textColor = textColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                // تحذير الدوران
                if (showRolloverWarning) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFFFFEBEE),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = ColorDanger,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "لاحظنا أن القراءة الحالية أقل من السابقة (دورة كاملة للعداد)",
                            color = ColorDanger,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "عدد مراتب العداد",
                        color = softTextColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { meterDigits = "5" }
                        ) {
                            RadioButton(
                                selected = meterDigits == "5",
                                onClick = { meterDigits = "5" }
                            )
                            Text("5 مراتب (99999)", fontSize = 12.sp)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { meterDigits = "6" }
                        ) {
                            RadioButton(
                                selected = meterDigits == "6",
                                onClick = { meterDigits = "6" }
                            )
                            Text("6 مراتب (999999)", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // زر الحساب
                Button(
                    onClick = {
                        val result = calculateBill(
                            category = selectedCategory,
                            meterType = selectedMeterType,
                            previousReading = previousReading,
                            currentReading = currentReading,
                            ctFactor = ctFactor,
                            meterDigits = meterDigits,
                            previousDate = previousDate,
                            currentDate = currentDate
                        )

                        if (result.error != null) {
                            errorMessage = result.error
                            billResult = null
                        } else {
                            errorMessage = ""
                            billResult = result
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = Color(0xFF2A1E00)
                    )
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "احسب الفاتورة",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                // رسالة الخطأ
                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        errorMessage,
                        color = ColorDanger,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFFFFEBEE),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(10.dp)
                    )
                }
            }
        }

        // النتيجة
        billResult?.let { result ->
            item {
                ResultCard(
                    result = result,
                    surfaceColor = surfaceColor,
                    textColor = textColor,
                    softTextColor = softTextColor,
                    accentColor = accentColor,
                    accentDarkColor = accentDarkColor
                )
            }
        }

        // Footer
        item {
            Text(
                "الأسعار حسب تعرفة وزارة الكهرباء المعتمدة — نتيجة تقديرية",
                color = softTextColor,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun CardComponent(
    surfaceColor: Color,
    softTextColor: Color,
    accentDarkColor: Color,
    content: @Composable (ColumnScope.() -> Unit)
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(5.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun SectionTitle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    softTextColor: Color,
    accentDarkColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = accentDarkColor,
            modifier = Modifier.size(19.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            title,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

@Composable
fun CategoryButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    accentColor: Color,
    accentDarkColor: Color,
    softTextColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(70.dp),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) accentColor else Color(0xFFF5F5F5),
            contentColor = if (isSelected) Color(0xFF2A1E00) else softTextColor
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    softTextColor: Color,
    accentColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            label,
            color = softTextColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedTextColor = textColor,
                unfocusedTextColor = textColor
            )
        )
    }
}

@Composable
fun ResultCard(
    result: BillResult,
    surfaceColor: Color,
    textColor: Color,
    softTextColor: Color,
    accentColor: Color,
    accentDarkColor: Color
) {
    CardComponent(
        surfaceColor = surfaceColor,
        softTextColor = softTextColor,
        accentDarkColor = accentDarkColor
    ) {
        // الإجمالي
        Text(
            "قيمة الفاتورة التقديرية",
            color = softTextColor,
            fontSize = 13.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Text(
            "${formatNumber(result.total.roundToInt())} دينار",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ColorNavy,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // الإحصائيات الصغيرة
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StatBox(
                label = "الاستهلاك",
                value = "${formatNumber(result.consumption.roundToInt())} ك.و.س",
                surfaceColor = Color(0xFFF5F5F5),
                textColor = textColor,
                softTextColor = softTextColor
            )
            StatBox(
                label = "متوسط الاستهلاك اليومي",
                value = result.dailyAverage,
                surfaceColor = Color(0xFFF5F5F5),
                textColor = textColor,
                softTextColor = softTextColor
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // جدول الشرائح
        SectionTitle(
            icon = Icons.Default.ShowChart,
            title = "تفاصيل الاحتساب حسب الشرائح",
            softTextColor = softTextColor,
            accentDarkColor = accentDarkColor
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // رأس الجدول
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp, 8.dp, 0.dp, 0.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("البند", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
                Text("الكمية", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                Text("المعامل", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                Text("الناتج", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
            }

            // صفوف البيانات
            result.breakdowns.forEachIndexed { index, line ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (index % 2 == 0) Color.Transparent else Color(0xFFFAFAFA)
                        )
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(line.label, fontSize = 11.sp, modifier = Modifier.weight(1.5f))
                    Text(formatNumber(line.quantity.roundToInt()), fontSize = 11.sp, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                    Text(formatNumber(line.rate.roundToInt()), fontSize = 11.sp, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                    Text(formatNumber(line.amount.roundToInt()), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
                }
            }
        }
    }
}

@Composable
fun StatBox(
    label: String,
    value: String,
    surfaceColor: Color,
    textColor: Color,
    softTextColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .weight(1f)
            .background(surfaceColor, RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = softTextColor, fontSize = 11.sp, textAlign = TextAlign.Center)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textColor, textAlign = TextAlign.Center)
    }
}

data class BillResult(
    val total: Double = 0.0,
    val consumption: Double = 0.0,
    val dailyAverage: String = "—",
    val breakdowns: List<LineItem> = emptyList(),
    val error: String? = null
)

data class LineItem(
    val label: String,
    val quantity: Double,
    val rate: Double,
    val amount: Double
)

fun calculateBill(
    category: String,
    meterType: String,
    previousReading: String,
    currentReading: String,
    ctFactor: String,
    meterDigits: String,
    previousDate: String,
    currentDate: String
): BillResult {
    val prev = previousReading.toDoubleOrNull()
    val curr = currentReading.toDoubleOrNull()

    if (prev == null || prev < 0) {
        return BillResult(error = "أدخل القراءة السابقة أولاً.")
    }
    if (curr == null || curr < 0) {
        return BillResult(error = "أدخل القراءة الحالية أولاً.")
    }

    val rawDifference = if (curr >= prev) {
        curr - prev
    } else {
        val maxValue = if (meterDigits == "5") 99999.0 else 999999.0
        (maxValue - prev) + curr
    }

    var multiplier = 1.0
    if (meterType == "C.T") {
        multiplier = ctFactor.toDoubleOrNull() ?: return BillResult(error = "أدخل معامل الضرب الخاص بمقياس C.T أولاً.")
        if (multiplier <= 0) {
            return BillResult(error = "معامل C.T يجب أن يكون أكبر من صفر.")
        }
    }

    val consumption = rawDifference * multiplier

    val breakdowns = mutableListOf<LineItem>()
    var total = 0.0

    // إضافة صف C.T إذا لزم الأمر
    if (meterType == "C.T") {
        breakdowns.add(
            LineItem(
                label = "فرق القراءة × معامل C.T",
                quantity = rawDifference,
                rate = multiplier,
                amount = consumption
            )
        )
    }

    // حساب الفاتورة حسب الشرائح أو الأسعار الثابتة
    when (category) {
        "منزلي" -> {
            val slabs = listOf(
                "من 1 إلى 1500" to (1500.0 to 10.0),
                "من 1501 إلى 3000" to (1500.0 to 35.0),
                "من 3001 إلى 4000" to (1000.0 to 80.0),
                "4001 فأكثر" to (Double.POSITIVE_INFINITY to 120.0)
            )
            var remaining = consumption
            for ((label, slab) in slabs) {
                if (remaining <= 0) break
                val (size, rate) = slab
                val used = minOf(remaining, size)
                val amount = used * rate
                breakdowns.add(LineItem(label, used, rate, amount))
                total += amount
                remaining -= used
            }
        }
        "تجاري" -> {
            val slabs = listOf(
                "من 1 إلى 1000" to (1000.0 to 60.0),
                "من 1001 إلى 2000" to (1000.0 to 80.0),
                "2001 فأكثر" to (Double.POSITIVE_INFINITY to 120.0)
            )
            var remaining = consumption
            for ((label, slab) in slabs) {
                if (remaining <= 0) break
                val (size, rate) = slab
                val used = minOf(remaining, size)
                val amount = used * rate
                breakdowns.add(LineItem(label, used, rate, amount))
                total += amount
                remaining -= used
            }
        }
        "زراعي", "صناعي", "حكومي" -> {
            val rate = when (category) {
                "حكومي" -> 120.0
                else -> 60.0
            }
            total = consumption * rate
            breakdowns.add(
                LineItem(
                    label = "كامل الاستهلاك",
                    quantity = consumption,
                    rate = rate,
                    amount = total
                )
            )
        }
    }

    // حساب متوسط الاستهلاك اليومي
    val dailyAverage = try {
        if (previousDate.isNotEmpty() && currentDate.isNotEmpty()) {
            val d1 = java.time.LocalDate.parse(previousDate)
            val d2 = java.time.LocalDate.parse(currentDate)
            val days = java.time.temporal.ChronoUnit.DAYS.between(d1, d2).toInt()
            if (days > 0) {
                "${String.format("%.1f", consumption / days)} ك.و.س/يوم"
            } else {
                "—"
            }
        } else {
            "—"
        }
    } catch (e: Exception) {
        "—"
    }

    return BillResult(
        total = total,
        consumption = consumption,
        dailyAverage = dailyAverage,
        breakdowns = breakdowns
    )
}

fun formatNumber(number: Int): String {
    return String.format("%,d", number).replace(",", ",")
}
