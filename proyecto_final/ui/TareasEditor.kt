package com.daviddj.proyecto_final_djl.ui

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.daviddj.proyecto_final_djl.AppViewModelProvider
import com.daviddj.proyecto_final_djl.ComposeFileProvider
import com.daviddj.proyecto_final_djl.R
import com.daviddj.proyecto_final_djl.VideoPlayer
import com.daviddj.proyecto_final_djl.viewModel.NotaDetails
import com.daviddj.proyecto_final_djl.viewModel.NotasEditorViewModel
import com.daviddj.proyecto_final_djl.viewModel.TareaDetails
import com.daviddj.proyecto_final_djl.viewModel.TareaUiState
import com.daviddj.proyecto_final_djl.viewModel.TareasEditorViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale
import kotlin.reflect.KFunction2

@OptIn(ExperimentalPermissionsApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EditorTareas(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel : TareasEditorViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navController: NavHostController,
    alarmScheduler: AlarmScheduler,
    onClickStGra: () -> Unit,
    onClickSpGra: () -> Unit,
    onClickStRe: () -> Unit,
    onClickSpRe: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedDate by rememberSaveable { mutableStateOf("") }
    var selectedTime by rememberSaveable { mutableStateOf("") }
    var isDateSelected by rememberSaveable { mutableStateOf(false) }
    var isTimeSelected by rememberSaveable { mutableStateOf(false) }

    var imageUris by remember { mutableStateOf(listOf<Uri>()) }
    var videoUris by remember { mutableStateOf(listOf<Uri>()) }

    //MULTIMEDIA
    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    var videoUri by remember {
        mutableStateOf<Uri?>(null)
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                imageUris = imageUris.plus(uri!!)
                viewModel.imageUris=viewModel.imageUris.plus(uri!!)
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && imageUri != null) {
                imageUris = imageUris.plus(imageUri!!)
                viewModel.imageUris=viewModel.imageUris.plus(imageUri!!)
            }
        }
    )

    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo(),
        onResult = { success ->
            if (success && videoUri != null) {
                videoUris = videoUris.plus(videoUri!!)
                viewModel.videoUris=viewModel.videoUris.plus(videoUri!!)
            }
        }
    )

    val context = LocalContext.current
    //MULTIMEDIA

    //ALARMA
    var secondText by remember {
        mutableStateOf("")
    }
    var messageText by remember {
        mutableStateOf("")
    }
    var alarmItem : AlarmItem? = null
    //ALARMA

    //AUDIO
    val recordAudioPermissionState = rememberPermissionState(
        Manifest.permission.RECORD_AUDIO
    )

    //Realiza un seguimiento del estado del diálogo de justificación, necesario cuando el usuario requiere más justificación
    var rationaleState by remember {
        mutableStateOf<RationaleState?>(null)
    }
    //AUDIO

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            IconButton(onClick = {
                navController.navigate(Routes.TareasScreen.route)
            }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Regresar",
                    modifier = Modifier
                        .size(32.dp)
                        .padding(0.dp)
                )
            }
            TimePicker(onTimeSelected = {
                selectedTime = it
                isTimeSelected = true
            },  isEnabled = isDateSelected)
            DatePicker(onDateSelected = {
                selectedDate = it
                isDateSelected = true
            })
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(R.string.recordatorio),fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                viewModel.updateMensaje(viewModel.tareaUiState.tareaDetails.name)
                alarmItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    AlarmItem(
                        LocalDateTime.now().plusSeconds(1000),
                        viewModel.mensaje.value,
                        selectedDate,
                        selectedTime
                    )
                } else {
                    TODO("VERSION.SDK_INT < O")
                }
                alarmItem?.let(alarmScheduler::schedule)
                secondText = "AAAA"
                messageText = "BBB"

            }, enabled = isTimeSelected) {
                Text(text = stringResource(R.string.reminder))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                alarmItem?.let(alarmScheduler::cancel)
            }, enabled = isTimeSelected) {
                Text(text = stringResource(R.string.cancelar))
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        TareaEntryBody(
            tareaUiState = viewModel.tareaUiState,
            onTareaValueChange = { updatedTareaDetails ->
                val combinedDateTime = "$selectedDate $selectedTime"
                viewModel.updateUiState(updatedTareaDetails, combinedDateTime)
            },
            onSaveClick = {
                coroutineScope.launch {
                    val combinedDateTime = "$selectedDate $selectedTime"
                    viewModel.updateUiState(viewModel.tareaUiState.tareaDetails, combinedDateTime)
                    viewModel.saveTarea()
                    navigateBack()
                }
            },
            modifier = Modifier
                .padding(5.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))        //MOSTRAR MULTIMEDIA
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    val uri = ComposeFileProvider.getImageUri(context)
                    imageUri = uri
                    cameraLauncher.launch(uri)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)) {
                Image(
                    modifier = Modifier
                        .size(35.dp)
                        .padding(4.dp),
                    painter = painterResource(R.drawable.camara_fotografica),
                    contentDescription = null
                )
            }
            Button(
                onClick = {
                    val uri = ComposeFileProvider.getVideoUri(context)
                    videoUri = uri
                    videoLauncher.launch(uri)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)) {
                Image(
                    modifier = Modifier
                        .size(35.dp)
                        .padding(4.dp),
                    painter = painterResource(R.drawable.video),
                    contentDescription = null
                )
            }
            Button(onClick = { imagePicker.launch("image/*") },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)) {
                Image(
                    modifier = Modifier
                        .size(35.dp)
                        .padding(4.dp),
                    painter = painterResource(R.drawable.carpeta),
                    contentDescription = null
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))//AUDIOS
        Row{
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // Show rationale dialog when needed
                rationaleState?.run { PermissionRationaleDialog(rationaleState = this) }
                PermissionRequestButtonTareas(
                    isGranted = recordAudioPermissionState.status.isGranted,
                    title = stringResource(R.string.record_audio),
                    onClickStGra,
                    onClickSpGra,
                    viewModel.audioUris,
                    viewModel
                ){
                    if (recordAudioPermissionState.status.shouldShowRationale) {
                        rationaleState = RationaleState(
                            "Permiso para grabar audio",
                            "In order to use this feature please grant access by accepting " + "the grabar audio dialog." + "\n\nWould you like to continue?",
                        ) { proceed ->
                            if (proceed) {
                                recordAudioPermissionState.launchPermissionRequest()
                            }
                            rationaleState = null
                        }
                    } else {
                        recordAudioPermissionState.launchPermissionRequest()
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))//MOSTRAR MULTIMEDIA
        Box(modifier = modifier) {
            LazyColumn(modifier = Modifier.align(Alignment.Center)) {
                items(imageUris + videoUris + viewModel.audioUris) { uri ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Column {
                            if (uri in imageUris) {
                                AsyncImage(
                                    model = uri,
                                    modifier = Modifier
                                        .height(400.dp)
                                        .fillMaxWidth(),
                                    contentDescription = "Selected image",
                                )
                            } else if (uri in videoUris) {
                                VideoPlayer(
                                    videoUri = uri,
                                    modifier = Modifier
                                        .height(400.dp)
                                        .fillMaxWidth()
                                )
                            } else if (uri in viewModel.audioUris) {
                                val audioPlayer2 = AndroidAudioPlayer2(context,uri)
                                PermissionRequestButton2(
                                    isGranted = recordAudioPermissionState.status.isGranted,
                                    title = stringResource(R.string.record_audio),
                                    onClickSpGra,
                                    onClickStRe,
                                    onClickStRe = {
                                        audioPlayer2.start(uri)
                                    },
                                    onClickSpRe = {
                                        audioPlayer2.stop()
                                    },
                                    viewModel.audioUris,
                                ){
                                    if (recordAudioPermissionState.status.shouldShowRationale) {
                                        rationaleState = RationaleState(
                                            "Permiso para grabar audio",
                                            "In order to use this feature please grant access by accepting " + "the grabar audio dialog." + "\n\nWould you like to continue?",
                                        ) { proceed ->
                                            if (proceed) {
                                                recordAudioPermissionState.launchPermissionRequest()
                                            }
                                            rationaleState = null
                                        }
                                    } else {
                                        recordAudioPermissionState.launchPermissionRequest()
                                    }
                                }
                            }
                            Button(
                                onClick = {
                                    // Elimina la tarjeta y quita la imagen del arreglo.
                                    imageUris = imageUris.filter { it != uri }
                                    videoUris = videoUris.filter { it != uri }
                                    viewModel.removeUri(uri)
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Image(
                                    modifier = Modifier
                                        .size(25.dp)
                                        .padding(2.dp),
                                    painter = painterResource(R.drawable.eliminar),
                                    contentDescription = null
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PermissionRequestButtonTareas(
    isGranted: Boolean, title: String,
    onClickStGra: () -> Unit,
    onClickSpGra: () -> Unit,
    audioUris: List<Uri>,
    tareasEditorViewModel: TareasEditorViewModel,
    onClick: () -> Unit,
) {
    if (isGranted) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.Center)
        ){
            Button(onClick = {
                onClickStGra()
            },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)) {
                //Text("Grabar")
                Text(
                    text = stringResource(R.string.AudioGrabar),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            Button(onClick = {
                onClickSpGra()
                audioUris.plus(audioUri!!)
                tareasEditorViewModel.audioUris = tareasEditorViewModel.audioUris.plus(audioUri!!)
                Log.e("URI en boton stop",audioUri.toString())
            },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)) {
                Text(
                    text = stringResource(R.string.Audio),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    } else {
        Button(onClick = onClick) {
            Text("Request $title")
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TareaEntryBody(
    tareaUiState: TareaUiState,
    onTareaValueChange: (TareaDetails) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
){
    Column() {
        TareaInputForm(
            tareaDetails = tareaUiState.tareaDetails,
            onValueChange = onTareaValueChange,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onSaveClick,
            enabled = tareaUiState.isEntryValid,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.save_action))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TareaInputForm(
    tareaDetails: TareaDetails,
    modifier: Modifier = Modifier,
    onValueChange: (TareaDetails) -> Unit = {},
    enabled: Boolean = true
) {
    Column(
    ) {
        OutlinedTextField(
            value = tareaDetails.name,
            onValueChange = { onValueChange(tareaDetails.copy(name = it))},
            label = { Text(stringResource(R.string.titulo)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true
        )
        val currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        Text(
            text = currentDateTime,
            style = MaterialTheme.typography.bodySmall
        )
        OutlinedTextField(
            value = tareaDetails.contenido,
            onValueChange = { onValueChange(tareaDetails.copy(contenido = it)) },
            label = { Text(stringResource(R.string.contenido)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = false
        )
    }
}



@Composable
fun DatePicker(onDateSelected: (String) -> Unit) {
    var fecha by rememberSaveable { mutableStateOf("") }
    val anio: Int
    val mes: Int
    val dia: Int
    val mCalendar: Calendar = Calendar.getInstance()
    anio = mCalendar.get(Calendar.YEAR)
    mes = mCalendar.get(Calendar.MONTH)
    dia = mCalendar.get(Calendar.DAY_OF_MONTH)

    val mDatePickerDialog = DatePickerDialog(
        LocalContext.current,
        { _, anio: Int, mes: Int, dia: Int ->
            // Formatear el día y el mes para asegurarse de que siempre tengan dos dígitos
            val formattedDay = String.format("%02d", dia)
            val formattedMonth = String.format("%02d", mes + 1)
            fecha = "$anio-$formattedMonth-$formattedDay"
            onDateSelected(fecha)
        },
        anio,
        mes,
        dia
    )

    Icon(
        imageVector = Icons.Filled.DateRange,
        contentDescription = "Recordatorios",
        modifier = Modifier
            .size(32.dp)
            .padding(0.dp)
            .clickable {
                mDatePickerDialog.show()
            }
    )
}

@Composable
fun TimePicker(onTimeSelected: (String) -> Unit, isEnabled: Boolean) {
    var hora by rememberSaveable { mutableStateOf("") }
    val mCalendar: Calendar = Calendar.getInstance()
    val hour = mCalendar[Calendar.HOUR_OF_DAY]
    val minute = mCalendar[Calendar.MINUTE]
    val second = mCalendar[Calendar.SECOND]

    val timePickerDialog = TimePickerDialog(
        LocalContext.current,
        { _, hourOfDay: Int, minuteOfDay: Int ->
            // Formatear la hora y los minutos, y asignar siempre "00" a los segundos
            val formattedHour = String.format("%02d", hourOfDay)
            val formattedMinute = String.format("%02d", minuteOfDay)
            hora = "$formattedHour:$formattedMinute:00"
            onTimeSelected(hora)
        }, hour, minute, false
    )

    if (isEnabled) {
        Icon(
            imageVector = Icons.Filled.Notifications,
            contentDescription = "Recordatorios",
            modifier = Modifier
                .size(32.dp)
                .padding(0.dp)
                .clickable {
                    timePickerDialog.show()
                }
        )
    } else {
        Icon(
            imageVector = Icons.Filled.Notifications,
            contentDescription = "Recordatorios (Deshabilitado)",
            modifier = Modifier
                .size(32.dp)
                .padding(0.dp)
                .alpha(0.5f) // Opacidad
        )
    }
}

//NOTIFICACIONES
class AlarmReceiverPerro : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val message = intent?.getStringExtra("EXTRA_MESSAGE") ?: return
        val channelId = "alarm_id"
        context?.let { ctx ->
            val notificationManager =
                ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val mensaje = "Revisa tu app, tienes pendiente: $message"

            val mensajeSpannable = SpannableString(mensaje)
            val start = mensaje.indexOf(message)
            val end = start + message.length
            mensajeSpannable.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            val builder = NotificationCompat.Builder(ctx, channelId)
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentTitle("Aplicación de Notas")
                .setContentText(mensajeSpannable)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
            notificationManager.notify(1, builder.build())
        }
    }
}

data class AlarmItem(
    val alarmTime : LocalDateTime,
    val message : String,
    val date: String,
    val time: String
)

interface AlarmScheduler {
    fun schedule(alarmItem: AlarmItem)
    fun cancel(alarmItem: AlarmItem)
}

class AlarmSchedulerImpl(
    private val context: Context
) : AlarmScheduler{

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @RequiresApi(Build.VERSION_CODES.O)
    override fun schedule(alarmItem: AlarmItem) {
        val intent = Intent(context, AlarmReceiverPerro::class.java).apply {
            putExtra("EXTRA_MESSAGE", alarmItem.message)
        }

        // Calculate milliseconds from selectedDate and selectedTime
        val combinedDateTime = "${alarmItem.date} ${alarmItem.time}"
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val dateTime = LocalDateTime.parse(combinedDateTime, dateTimeFormatter)
        val milliseconds = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // Set the alarm with the calculated milliseconds
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            milliseconds,
            PendingIntent.getBroadcast(
                context,
                alarmItem.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        Log.e("Alarm", "Alarm set at $combinedDateTime")
    }

    override fun cancel(alarmItem: AlarmItem) {
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                alarmItem.hashCode(),
                Intent(context, AlarmReceiverPerro::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }
}
