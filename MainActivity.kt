package com.example.ardayapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.ardayapp.data.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

class MainActivity : ComponentActivity() {
    private val dao by lazy { SchoolDatabase.get(this).dao() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ArdayApp(dao) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArdayApp(dao: SchoolDao) {
    var tab by remember { mutableIntStateOf(0) }
    var classes by remember { mutableStateOf(listOf<SchoolClass>()) }
    var students by remember { mutableStateOf(listOf<Student>()) }
    var selectedClass by remember { mutableStateOf<Long?>(null) }
    var present by remember { mutableIntStateOf(0) }
    var absent by remember { mutableIntStateOf(0) }
    var refresh by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    fun reload() { scope.launch {
        classes = dao.getClasses(); students = dao.getStudents()
        if (selectedClass == null && classes.isNotEmpty()) selectedClass = classes.first().id
        present = dao.presentCount(today()); absent = dao.absentCount(today())
    } }
    LaunchedEffect(refresh) { reload() }
    MaterialTheme(colorScheme = lightColorScheme(primary = androidx.compose.ui.graphics.Color(0xFF176B55), secondary = androidx.compose.ui.graphics.Color(0xFFDAA520))) {
        Scaffold(
            topBar = { TopAppBar(title = { Text("Arday & Xaadirin") }, actions = { Text(today(), modifier = Modifier.padding(end = 16.dp), style = MaterialTheme.typography.labelMedium) }) },
            bottomBar = { NavigationBar {
                NavigationBarItem(selected = tab == 0, onClick = { tab = 0 }, icon = { Icon(Icons.Default.People, null) }, label = { Text("Ardayda") })
                NavigationBarItem(selected = tab == 1, onClick = { tab = 1 }, icon = { Icon(Icons.Default.CheckCircle, null) }, label = { Text("Xaadirin") })
                NavigationBarItem(selected = tab == 2, onClick = { tab = 2 }, icon = { Icon(Icons.Default.Assessment, null) }, label = { Text("Warbixin") })
            } }
        ) { padding ->
            when (tab) {
                0 -> RegistrationScreen(Modifier.padding(padding), classes, students, selectedClass, { selectedClass = it }, dao) { refresh++ }
                1 -> AttendanceScreen(Modifier.padding(padding), classes, selectedClass, { selectedClass = it }, dao, students) { refresh++ }
                else -> ReportScreen(Modifier.padding(padding), students.size, classes.size, present, absent)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(modifier: Modifier, classes: List<SchoolClass>, students: List<Student>, selected: Long?, onSelect: (Long) -> Unit, dao: SchoolDao, onChanged: () -> Unit) {
    var showStudent by remember { mutableStateOf(false) }
    var showClass by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Diiwaangelinta ardayda", style = MaterialTheme.typography.headlineSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { showStudent = true }, enabled = classes.isNotEmpty(), modifier = Modifier.weight(1f)) { Icon(Icons.Default.PersonAdd, null); Spacer(Modifier.width(6.dp)); Text("Arday cusub") }
            OutlinedButton(onClick = { showClass = true }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Add, null); Text("Class cusub") }
        }
        OutlinedTextField(value = search, onValueChange = { search = it }, label = { Text("Raadi arday") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Search, null) })
        Text("Classes: ${classes.size}  •  Arday: ${students.size}", style = MaterialTheme.typography.titleSmall)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(students.filter { it.fullName.contains(search, true) || it.studentCode.contains(search, true) }) { student ->
                val className = classes.firstOrNull { it.id == student.classId }?.name ?: "Class lama yaqaan"
                Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) {
                    Text(student.fullName, style = MaterialTheme.typography.titleMedium)
                    Text("ID: ${student.studentCode}  •  $className")
                    if (student.phone.isNotBlank()) Text("Tel: ${student.phone}", style = MaterialTheme.typography.bodySmall)
                } }
            }
        }
    }
    if (showClass) ClassDialog(onDismiss = { showClass = false }) { name ->
        if (name.isNotBlank()) { kotlinx.coroutines.MainScope().launch { dao.addClass(SchoolClass(name = name.trim())); onChanged() } }; showClass = false
    }
    if (showStudent) StudentDialog(classes, selected, onDismiss = { showStudent = false }) { code, name, phone, classId ->
        kotlinx.coroutines.MainScope().launch { try { dao.addStudent(Student(studentCode = code.trim(), fullName = name.trim(), phone = phone.trim(), classId = classId)); onChanged() } catch (_: Exception) { } }
        showStudent = false
    }
}

@Composable
fun ClassDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Ku dar class") }, text = { OutlinedTextField(name, { name = it }, label = { Text("Magaca class-ka") }) }, confirmButton = { TextButton(onClick = { onSave(name) }) { Text("Kaydi") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Jooji") } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDialog(classes: List<SchoolClass>, selected: Long?, onDismiss: () -> Unit, onSave: (String, String, String, Long) -> Unit) {
    var code by remember { mutableStateOf("") }; var name by remember { mutableStateOf("") }; var phone by remember { mutableStateOf("") }
    var classId by remember { mutableStateOf(selected ?: classes.first().id) }; var expanded by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Diiwaangeli arday") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(code, { code = it }, label = { Text("Student ID") }, singleLine = true)
            OutlinedTextField(name, { name = it }, label = { Text("Magaca oo buuxa") }, singleLine = true)
            OutlinedTextField(phone, { phone = it }, label = { Text("Telefoon (ikhtiyaari)") }, singleLine = true)
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(classes.firstOrNull { it.id == classId }?.name ?: "Dooro class", {}, readOnly = true, label = { Text("Class") }, modifier = Modifier.menuAnchor().fillMaxWidth(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) })
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) { classes.forEach { c -> DropdownMenuItem(text = { Text(c.name) }, onClick = { classId = c.id; expanded = false }) } }
            }
        }
    }, confirmButton = { TextButton(enabled = code.isNotBlank() && name.isNotBlank(), onClick = { onSave(code, name, phone, classId) }) { Text("Diiwaangeli") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Jooji") } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(modifier: Modifier, classes: List<SchoolClass>, selected: Long?, onSelect: (Long) -> Unit, dao: SchoolDao, allStudents: List<Student>, onChanged: () -> Unit) {
    var members by remember { mutableStateOf(listOf<Student>()) }
    var records by remember { mutableStateOf(mapOf<Long, String>()) }
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(selected, allStudents) {
        if (selected != null) {
            members = dao.studentsInClass(selected)
            records = dao.attendanceForDate(today()).associate { it.studentId to it.status }
        } else { members = emptyList(); records = emptyMap() }
    }
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Xaadirinta ardayda", style = MaterialTheme.typography.headlineSmall)
        Text("Taariikh: ${today()}")
        if (classes.isEmpty()) Text("Marka hore samee class qaybta Ardayda.") else {
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(classes.firstOrNull { it.id == selected }?.name ?: "Dooro class", {}, readOnly = true, label = { Text("Dooro class") }, modifier = Modifier.menuAnchor().fillMaxWidth(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) })
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) { classes.forEach { c -> DropdownMenuItem(text = { Text(c.name) }, onClick = { onSelect(c.id); expanded = false }) } }
            }
            if (members.isEmpty()) Text("Class-kan arday kuma jiraan.")
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(members) { student ->
                    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) {
                        Text(student.fullName, style = MaterialTheme.typography.titleMedium)
                        Text(student.studentCode, style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val status = records[student.id]
                            FilterChip(selected = status == "Jooga", onClick = { scope.launch { dao.saveAttendance(Attendance(studentId = student.id, date = today(), status = "Jooga")); records = records + (student.id to "Jooga"); onChanged() } }, label = { Text("Jooga") }, leadingIcon = { Icon(Icons.Default.Check, null) })
                            FilterChip(selected = status == "Maqan", onClick = { scope.launch { dao.saveAttendance(Attendance(studentId = student.id, date = today(), status = "Maqan")); records = records + (student.id to "Maqan"); onChanged() } }, label = { Text("Maqan") }, leadingIcon = { Icon(Icons.Default.Close, null) })
                        }
                    } }
                }
            }
        }
    }
}

@Composable
fun ReportScreen(modifier: Modifier, studentCount: Int, classCount: Int, present: Int, absent: Int) {
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Warbixinta maanta", style = MaterialTheme.typography.headlineSmall)
        Text(today(), style = MaterialTheme.typography.bodyMedium)
        ReportCard("Wadarta ardayda", studentCount.toString(), Icons.Default.People)
        ReportCard("Wadarta classes", classCount.toString(), Icons.Default.Class)
        ReportCard("Jooga maanta", present.toString(), Icons.Default.CheckCircle)
        ReportCard("Maqan maanta", absent.toString(), Icons.Default.Cancel)
        Text("Fiiro gaar ah: xaadirinta waxaa lagu kaydiyaa telefoonkaaga, internet looma baahna.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun ReportCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(Modifier.fillMaxWidth()) { Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp)); Spacer(Modifier.width(16.dp))
        Column { Text(title, style = MaterialTheme.typography.bodyMedium); Text(value, style = MaterialTheme.typography.headlineMedium) }
    } }
}
