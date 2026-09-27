package com.electricfaw.billcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

private val Cream = Color(0xFFFFF6E0)
private val SurfaceSun = Color(0xFFFFFFFF)
private val Surface2Sun = Color(0xFFFFF0C7)
private val Navy = Color(0xFF1B2A4A)
private val Soft = Color(0xFF5B6B8C)
private val Gold = Color(0xFFE8A317)
private val GoldDark = Color(0xFFB67A0E)
private val Night = Color(0xFF0B1224)
private val NightSurface = Color(0xFF16213A)
private val NightSurface2 = Color(0xFF1C2A4A)
private val NightText = Color(0xFFEAF2FF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { BillApp() } }
}

@Composable
fun BillApp() {
    var night by remember { mutableStateOf(false) }
    val colors = if (night) darkColorScheme(primary = Gold, background = Night, surface = NightSurface, onSurface = NightText, onBackground = NightText, secondary = Color(0xFF9FB3D9)) else lightColorScheme(primary = GoldDark, background = Cream, surface = SurfaceSun, onSurface = Navy, onBackground = Navy, secondary = Soft)
    MaterialTheme(colorScheme = colors, typography = Typography(defaultFontFamily = FontFamily.SansSerif)) {
        CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
            BillScreen(night) { night = !night }
        }
    }
}

@Composable
private fun BillScreen(night: Boolean, toggleTheme: () -> Unit) {
    var category by remember { mutableStateOf("منزلي") }
    var meterType by remember { mutableStateOf("عادي") }
    var previous by remember { mutableStateOf("") }
    var current by remember { mutableStateOf("") }
    var factor by remember { mutableStateOf("") }
    var digits by remember { mutableStateOf("5") }
    var previousDate by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<BillResult?>(null) }
    val ctEligible = category != "منزلي" && category != "تجاري"
    val rollover = previous.toDoubleOrNull() != null && current.toDoubleOrNull() != null && current.toDouble() < previous.toDouble()
    val bg = if (night) Night else Cream
    val surface2 = if (night) NightSurface2 else Surface2Sun

    Surface(Modifier.fillMaxSize(), color = bg) {
        LazyColumn(contentPadding = PaddingValues(16.dp, 18.dp, 16.dp, 32.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { Header(night, toggleTheme) }
            item { CardBox { SectionTitle(Icons.Default.Business, "اختر نوع الاشتراك")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("منزلي" to Icons.Default.Home, "تجاري" to Icons.Default.Store, "زراعي" to Icons.Default.Spa).forEach { (name, icon) -> CategoryButton(name, icon, category == name) { category = name; meterType = "عادي" } } }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("صناعي" to Icons.Default.Factory, "حكومي" to Icons.Default.AccountBalance).forEach { (name, icon) -> CategoryButton(name, icon, category == name) { category = name; meterType = "عادي" }; Spacer(Modifier.weight(1f)) } }
            } }
            if (ctEligible) item { CardBox { SectionTitle(Icons.Default.Speed, "نوع المقياس")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) { listOf("عادي", "C.T").forEach { type -> CategoryButton(type, Icons.Default.Speed, meterType == type) { meterType = type } } }
                if (meterType == "C.T") { Spacer(Modifier.height(10.dp)); InputField("معامل الضرب المثبت على المقياس (نسبة C.T)", factor, { factor = it }, "مثال: 40") }
            } }
            item { CardBox { SectionTitle(Icons.Default.AvTimer, "قراءة العداد")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { Box(Modifier.weight(1f)) { InputField("القراءة السابقة", previous, { previous = it }, "مثال: 1500") }; Box(Modifier.weight(1f)) { InputField("القراءة الحالية", current, { current = it }, "مثال: 1800") } }
                Spacer(Modifier.height(8.dp)); Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { Box(Modifier.weight(1f)) { InputField("تاريخ القراءة السابقة", previousDate, { previousDate = it }, "YYYY-MM-DD") }; Box(Modifier.weight(1f)) { InputField("تاريخ القراءة الحالية", currentDate, { currentDate = it }, "YYYY-MM-DD") } }
                if (rollover) { Spacer(Modifier.height(10.dp)); Text("⚠ القراءة الحالية أقل من السابقة. تم تفعيل حساب دورة كاملة للعداد.", color = GoldDark, fontSize = 12.sp, modifier = Modifier.background(surface2, RoundedCornerShape(12.dp)).padding(10.dp))
                    Spacer(Modifier.height(8.dp)); Text("عدد مراتب العداد", color = Soft, fontSize = 13.sp); Row { listOf("5" to "5 مراتب (99999)", "6" to "6 مراتب (999999)").forEach { (v, label) -> Row(verticalAlignment = Alignment.CenterVertically) { RadioButton(digits == v, { digits = v }); Text(label, fontSize = 12.sp) } } }
                }
                Spacer(Modifier.height(14.dp)); Button(onClick = { val r = calculate(category, meterType, previous, current, factor, digits); if (r.error != null) { error = r.error; result = null } else { error = ""; result = r } }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color(0xFF2A1E00))) { Icon(Icons.Default.Calculate, null); Spacer(Modifier.width(8.dp)); Text("احسب الفاتورة", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                if (error.isNotEmpty()) Text(error, color = Color(0xFFC0392B), fontSize = 13.sp, modifier = Modifier.padding(top = 10.dp))
            } }
            result?.let { item { ResultCard(it) } }
            item { Text("الأسعار حسب تعرفة وزارة الكهرباء المعتمدة — نتيجة تقديرية", color = Soft, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
        }
    }
}

@Composable private fun Header(night: Boolean, toggle: () -> Unit) { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(46.dp).background(Gold, RoundedCornerShape(23.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Bolt, null, tint = Color.White, modifier = Modifier.size(30.dp)) }; Spacer(Modifier.width(12.dp)); Column { Text("احسب فاتورتك", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold); Text("حاسبة فاتورة الكهرباء", color = Soft, fontSize = 11.sp) } }; IconButton(onClick = toggle) { Icon(if (night) Icons.Default.DarkMode else Icons.Default.LightMode, "تبديل الثيم", tint = GoldDark) } } }
@Composable private fun CardBox(content: @Composable ColumnScope.() -> Unit) { Card(shape = RoundedCornerShape(18.dp), elevation = CardDefaults.cardElevation(5.dp), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), content = content) } }
@Composable private fun SectionTitle(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) { Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) { Icon(icon, null, tint = GoldDark, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(7.dp)); Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp) } }
@Composable private fun CategoryButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, onClick: () -> Unit) { Button(onClick, modifier = Modifier.weight(1f).height(72.dp), shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(3.dp), colors = ButtonDefaults.buttonColors(containerColor = if (selected) Gold else MaterialTheme.colorScheme.surfaceVariant, contentColor = if (selected) Color(0xFF2A1E00) else MaterialTheme.colorScheme.onSurfaceVariant)) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(icon, null, modifier = Modifier.size(24.dp)); Text(text, fontSize = 12.sp) } } }
@Composable private fun InputField(label: String, value: String, change: (String) -> Unit, hint: String) { OutlinedTextField(value, change, modifier = Modifier.fillMaxWidth(), label = { Text(label, fontSize = 12.sp) }, placeholder = { Text(hint, fontSize = 12.sp) }, singleLine = true, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal), shape = RoundedCornerShape(12.dp)) }

data class Line(val label: String, val quantity: Double, val rate: Double, val amount: Double)
data class BillResult(val total: Double = 0.0, val consumption: Double = 0.0, val daily: String = "—", val lines: List<Line> = emptyList(), val error: String? = null)

private fun calculate(category: String, type: String, pText: String, cText: String, factorText: String, digits: String): BillResult { val p = pText.toDoubleOrNull(); val c = cText.toDoubleOrNull(); if (p == null || p < 0) return BillResult(error = "أدخل القراءة السابقة أولاً."); if (c == null || c < 0) return BillResult(error = "أدخل القراءة الحالية أولاً."); val raw = if (c >= p) c - p else ((if (digits == "5") 99999 else 999999) - p) + c; var multiplier = 1.0; if (type == "C.T") { multiplier = factorText.toDoubleOrNull() ?: return BillResult(error = "أدخل معامل الضرب الخاص بمقياس C.T أولاً."); if (multiplier <= 0) return BillResult(error = "معامل C.T يجب أن يكون أكبر من صفر.") }; val consumption = raw * multiplier; val lines = mutableListOf<Line>(); var total = 0.0; if (category == "منزلي" || category == "تجاري") { val slabs = if (category == "منزلي") listOf("من 1 إلى 1500" to 1500.0 to 10.0, "من 1501 إلى 3000" to 1500.0 to 35.0, "من 3001 إلى 4000" to 1000.0 to 80.0, "4001 فأكثر" to Double.POSITIVE_INFINITY to 120.0) else listOf("من 1 إلى 1000" to 1000.0 to 60.0, "من 1001 إلى 2000" to 1000.0 to 80.0, "2001 فأكثر" to Double.POSITIVE_INFINITY to 120.0); var remaining = consumption; for (s in slabs) { if (remaining <= 0) break; val used = minOf(remaining, s.second); lines += Line(s.first, used, s.third, used * s.third); total += used * s.third; remaining -= used } } else { val rate = if (category == "حكومي") 120.0 else 60.0; total = consumption * rate; lines += Line("كامل الاستهلاك", consumption, rate, total) }; if (type == "C.T") lines.add(0, Line("فرق القراءة × معامل C.T", raw, multiplier, consumption)); return BillResult(total, consumption, "—", lines) }

@Composable private fun ResultCard(r: BillResult) { CardBox { Text("قيمة الفاتورة التقديرية", color = Soft, fontSize = 13.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center); Text("${fmt(r.total)} دينار", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Navy, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center); Spacer(Modifier.height(12.dp)); Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { Stat("الاستهلاك", "${fmt(r.consumption)} ك.و.س"); Stat("متوسط الاستهلاك اليومي", r.daily) }; Spacer(Modifier.height(16.dp)); SectionTitle(Icons.Default.ShowChart, "تفاصيل الاحتساب حسب الشرائح"); r.lines.forEach { line -> Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(line.label, fontSize = 11.sp, modifier = Modifier.weight(1.6f)); Text(fmt(line.quantity), fontSize = 11.sp, modifier = Modifier.weight(.8f), textAlign = TextAlign.Center); Text(fmt(line.rate), fontSize = 11.sp, modifier = Modifier.weight(.7f), textAlign = TextAlign.Center); Text(fmt(line.amount), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(.8f), textAlign = TextAlign.End) } } } }
@Composable private fun Stat(label: String, value: String) { Column(Modifier.weight(1f).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)).padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(label, color = Soft, fontSize = 11.sp, textAlign = TextAlign.Center); Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center) } }
private fun fmt(value: Double): String = NumberFormat.getNumberInstance(Locale.US).format(value.roundToInt())
