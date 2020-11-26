package cp.kt.selfcontrol.ui.fragment

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.PowerManager
import android.view.*
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.timePicker
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.jeremyliao.liveeventbus.LiveEventBus
import com.mt.mtloadingmanager.LoadingManager
import com.yhao.floatwindow.FloatWindow
import com.yhao.floatwindow.MoveType
import com.yhao.floatwindow.Screen
import cp.kt.selfcontrol.MainApplication
import cp.kt.selfcontrol.R
import cp.kt.selfcontrol.data.Dog
import cp.kt.selfcontrol.data.Pet
import cp.kt.selfcontrol.data.Rabbit
import cp.kt.selfcontrol.data.Squirrel
import cp.kt.selfcontrol.receiver.AlarmMonitorAppReceiver
import cp.kt.selfcontrol.service.AlarmMonitorAppService
import cp.kt.selfcontrol.service.CountDownService
import cp.kt.selfcontrol.service.MonitorAppService
import cp.kt.selfcontrol.ui.view.PetView
import cp.kt.selfcontrol.util.*
import java.util.*
import kotlin.math.max


class HomeFragment : Fragment() {
    private lateinit var petView: PetView
    // 模式切换开关
    private lateinit var modeSwitch: SwitchCompat

    // 倒计时背景
    private lateinit var progressBar: ProgressBar

    // 倒计时文本
    private lateinit var countdownTextView: TextView

    // 开始按钮
    private lateinit var startButton: Button

    // 倒计时时间
    private var countdown: Calendar = DateUtil.time2Calendar("00:00:00")
        set(value) {
            field = value

            val isScreenOn = powerManager?.isInteractive ?: true
            if (isScreenOn || isZeroTime()) { // 判断屏幕开关状态
                val time = DateUtil.calendar2time(value)
                countdownTextView.text = time
                petView.countdownTextView.text = time
            }
        }

    private val loadingManager: LoadingManager by lazy {
        LoadingManager(context)
    }

    private var powerManager: PowerManager? = null
    lateinit var alarmMonitorAppReceiver: AlarmMonitorAppReceiver

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        petView = PetView(view.context)
        modeSwitch = view.findViewById(R.id.modeSwitch)
        progressBar = view.findViewById(R.id.progressBar)
        countdownTextView = view.findViewById(R.id.main_countdownTextView)
        startButton = view.findViewById(R.id.startButton)

        initData()

        initListener()
        // 事件通知监听
        initEventBus()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        val item = menu.findItem(R.id.petItem)
        item.setIcon(
            when (SPUtil.get(Constant.SP.PET_TYPE, 0)) {
                1 -> R.drawable.dog
                2 -> R.drawable.squirrel
                else -> R.drawable.rabbit
            }
        )
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.petItem -> {
                context?.let {
                    val petItems = listOf(
                        it.getString(R.string.rabbit),
                        it.getString(R.string.dog),
                        it.getString(R.string.squirrel)
                    )
                    val initialSelection = SPUtil.get(Constant.SP.PET_TYPE, 0)
                    MaterialDialog(it).show {
                        listItemsSingleChoice(
                            items = petItems,
                            initialSelection = initialSelection
                        ) { _, index, _ ->
                            if (index != initialSelection) {
                                SPUtil.save(Constant.SP.PET_TYPE, index)

                                when (index) {
                                    1 -> petView.pet = Dog()
                                    2 -> petView.pet = Squirrel()
                                    else -> petView.pet = Rabbit()
                                }

                                item.setIcon(
                                    when (index) {
                                        1 -> R.drawable.dog
                                        2 -> R.drawable.squirrel
                                        else -> R.drawable.rabbit
                                    }
                                )
                            }
                        }
                    }
                }
                true
            }
            R.id.poemItem -> {
                context?.let {
                    MaterialDialog(it).show {
                        title(res = R.string.poem)
                        input(hintRes = R.string.hint_poem, maxLength = 5)
                        positiveButton { dialog ->
                            val inputField = dialog.getInputField()
                            if (inputField.text.toString().isNotBlank()) {
                                loadingManager.show()

                                Thread {
                                    BaiduHelper.getPoem(inputField.text.toString().trim())
                                }.start()
                            }
                        }
                        negativeButton()
                    }
                }
                true
            }
            R.id.settingItem -> {
                context?.let {
                    val monitorItems = listOf(
                        it.getString(R.string.accessibility_monitor),
                        it.getString(R.string.alarm_monitor)
                    )
                    val initialSelection = SPUtil.get(Constant.SP.MONITOR_MODE, 0)
                    MaterialDialog(it).show {
                        listItemsSingleChoice(
                            items = monitorItems,
                            initialSelection = initialSelection
                        ) { _, index, _ ->
                            if (index != initialSelection) {
                                SPUtil.save(Constant.SP.MONITOR_MODE, index)
                                SPUtil.get(Constant.SP.MONITOR_MODE, 0)
                            }
                        }
                    }
                }
                true
            }
            R.id.allowListItem -> {
                view?.findNavController()?.navigate(R.id.allowListFragment)
                true
            }
            R.id.aboutItem -> {
                view?.findNavController()?.navigate(R.id.aboutFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        context?.let {
            when (requestCode) {
                PermissionUtil.ACCESSIBILITY_CODE ->
                    if (resultCode == AppCompatActivity.RESULT_OK && PermissionUtil.canAccessibility(
                            it
                        )
                    ) {
                        startMonitorAppService()
                    } else {
                        Toast.makeText(
                            context, getString(R.string.no_permission_accessibility),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                PermissionUtil.USAGE_ACCESS_CODE ->
                    if (resultCode == AppCompatActivity.RESULT_OK && PermissionUtil.canUsageAccess(
                            it
                        )
                    ) {
                        startMonitorAppService()
                    } else {
                        Toast.makeText(
                            context, getString(R.string.no_permission_usage_access),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                PermissionUtil.OVERLAY_CODE ->
                    if (resultCode == AppCompatActivity.RESULT_OK && PermissionUtil.canDrawOverlays(
                            it
                        )
                    ) {
                        startFloatWindow()
                    } else {
                        Toast.makeText(
                            context, getString(R.string.no_permission_overlay),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }

    private fun initData() {
        context?.let {
            val flag = SPUtil.contains(Constant.SP.ALLOW_LIST)

            if (!flag) {
                val set = mutableSetOf<String>()
                set.add(it.packageName)
                for (app in AppHelper.getInstance(it).systemApps) {
                    set.add(app.processName)
                }

                SPUtil.saveSet(Constant.SP.ALLOW_LIST, set)
            }

            if (PermissionUtil.canDrawOverlays(it)) {
                startFloatWindow()

                petView.pet = when (SPUtil.get(Constant.SP.PET_TYPE, 0)) {
                    1 -> Dog()
                    2 -> Squirrel()
                    else -> Rabbit()// 事件监听
                }
                petView.reloadData()
            }
        }
    }

    private fun initListener() {
        modeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                modeSwitch.text = getString(R.string.clock_mode)
                countdown = DateUtil.time2Calendar("00:00:00")
            } else {
                modeSwitch.text = getString(R.string.countdown_mode)
                countdown = DateUtil.time2Calendar("00:00:00")
            }
        }

        progressBar.setOnClickListener {
            alertTimePicker()
        }

        countdownTextView.setOnClickListener {
            alertTimePicker()
        }

        startButton.setOnClickListener {
            start()

        }
    }

    private fun initEventBus() {
        LiveEventBus
            .get(Constant.EventBus.APP_STATE_CHANGED, String::class.java)
            .observeForever { packageName -> reloadPet(packageName) }

        LiveEventBus
            .get(Constant.EventBus.COUNTDOWN_CHANGED, Long::class.java)
            .observeForever { countdown -> reloadCountdown(countdown) }

        LiveEventBus
            .get(Constant.EventBus.POEM, String::class.java)
            .observeForever { poem -> alertPoem(poem) }
    }

    private fun start() {
        if (isZeroTime()) {
            Toast.makeText(context, getString(R.string.no_picker_study_time), Toast.LENGTH_SHORT)
                .show()
            return
        }

        context?.let {
            if (!PermissionUtil.canDrawOverlays(it)) {

                MaterialDialog(it).show {
                    title(R.string.no_permission)
                    message(R.string.no_permission_overlay)
                    positiveButton {
                        PermissionUtil.requestOverlayPermission(this@HomeFragment)
                    }
                    negativeButton()
                }

                return
            }

            val monitorMode = SPUtil.get(Constant.SP.MONITOR_MODE, 0) == 0
            if (monitorMode) {
                if (!PermissionUtil.canAccessibility(it)) {
                    MaterialDialog(it).show {
                        title(R.string.no_permission)
                        message(R.string.no_permission_accessibility)
                        positiveButton {
                            PermissionUtil.requestAccessibilityPermission(this@HomeFragment)
                        }
                        negativeButton()
                    }
                    return
                }
            } else {
                if (!PermissionUtil.canUsageAccess(it)) {
                    MaterialDialog(it).show {
                        title(R.string.no_permission)
                        message(R.string.no_permission_usage_access)
                        positiveButton {
                            PermissionUtil.requestUsageAccessPermission(this@HomeFragment)
                        }
                        negativeButton()
                    }
                    return
                }
            }

            startFloatWindow()
            startMonitorAppService()
            startCountdownService(DateUtil.time2Second(DateUtil.calendar2time(countdown)) * 1000)

            petView.state = Pet.State.Studying

            powerManager = it.getSystemService(Context.POWER_SERVICE) as PowerManager
        }
    }

    private fun alertTimePicker() {
        val currentTime = if (!modeSwitch.isChecked) {
            countdown
        } else {
            Calendar.getInstance()
        }

        val title = if (!modeSwitch.isChecked) {
            getString(R.string.countdown_mode)
        } else {
            getString(R.string.clock_mode)
        }

        context?.let {
            MaterialDialog(it).show {
                title(text = title)
                timePicker(currentTime = currentTime) { _, time ->
                    countdown = if (!modeSwitch.isChecked) {
                        time
                    } else {
                        val diff = max(0, time.timeInMillis - Calendar.getInstance().timeInMillis)
                        DateUtil.time2Calendar(DateUtil.second2Time(diff / 1000))
                    }
                    petView.state = Pet.State.Idle
                }
            }
        }
    }

    private fun alertPoem(poem: String) {
        loadingManager.hide(null)

        val temp = poem.split("#")
        val title = temp.first()
        val content = temp.last()

        if (title.isBlank() && content.isBlank()) {
            Toast.makeText(
                context, getString(R.string.poem_error),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        context?.let {
            MaterialDialog(it).show {
                title(text = title)
                message(text = content)
                positiveButton()
            }
        }
    }

    private fun startMonitorAppService() {
        val monitorMode = SPUtil.get(Constant.SP.MONITOR_MODE, 0) == 0
        if (monitorMode) {
            Intent(context, MonitorAppService::class.java).also { intent ->
                context?.startService(intent)
            }
        } else {
            Intent(context, AlarmMonitorAppService::class.java).also { intent ->
                context?.startService(intent)
            }

            registerAlarmMonitorAppReceiver()
        }
    }

    private fun stopMonitorAppService() {
        val monitorMode = SPUtil.get(Constant.SP.MONITOR_MODE, 0) == 0
        if (monitorMode) {
            Intent(context, MonitorAppService::class.java).also { intent ->
                context?.stopService(intent)
            }
        } else {
            Intent(context, AlarmMonitorAppService::class.java).also { intent ->
                context?.stopService(intent)
            }

            unregisterAlarmMonitorAppReceiver()
        }
    }

    private fun registerAlarmMonitorAppReceiver() {
        alarmMonitorAppReceiver = AlarmMonitorAppReceiver()
        val filter = IntentFilter()
        context?.registerReceiver(alarmMonitorAppReceiver, filter)
    }

    private fun unregisterAlarmMonitorAppReceiver() {
        context?.unregisterReceiver(alarmMonitorAppReceiver)
    }

    private fun startCountdownService(countdown: Long) {
        Intent(context, CountDownService::class.java).also { intent ->
            intent.putExtra(Constant.Extra.COUNTDOWN, countdown)
            context?.startService(intent)
        }
    }

    private fun stopCountdownService() {
        Intent(context, CountDownService::class.java).also { intent ->
            context?.stopService(intent)
        }
    }

    private fun startFloatWindow() {
        if (FloatWindow.get() != null) {
            FloatWindow.get().show()
            return
        }
        context?.applicationContext?.let {
            FloatWindow
                .with(it)
                .setView(petView)
                .setWidth(WindowManager.LayoutParams.WRAP_CONTENT)
                .setHeight(WindowManager.LayoutParams.WRAP_CONTENT)
                .setX(0)
                .setY(Screen.height, 0.5f)
                .setDesktopShow(true)
                .setMoveType(MoveType.slide)
                .build()
        }

        FloatWindow.get().show()
    }

//    private fun stopFloatWindow() {
//        FloatWindow.get().hide()
//    }

    // 模拟点击Home键，将屏幕退回到主界面，以此模拟实现关闭效果
    private fun closeApp() {
        val intent = Intent()
        intent.action = Intent.ACTION_MAIN
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        MainApplication.context.startActivity(intent)
    }

    private fun reloadPet(packageName: String) {
        val isScreenOn = powerManager?.isInteractive ?: true
        if (!isScreenOn) return // 判断屏幕开关状态

        if (isZeroTime()) {
            petView.state = Pet.State.Idle

            stopMonitorAppService()
            stopCountdownService()
        } else {
            petView.state = Pet.State.Warn

            val flag = SPUtil.getSet(Constant.SP.ALLOW_LIST)?.contains(packageName) ?: false
            if (!flag) {
                closeApp()
            }
        }
    }

    private fun reloadCountdown(countdown: Long) {
        if (countdown <= 0L) {
            stopMonitorAppService()
            stopCountdownService()

            petView.state = Pet.State.Studied
        }

        this.countdown = DateUtil.time2Calendar(DateUtil.second2Time(countdown / 1000))
    }

    private fun isZeroTime(): Boolean {
        return (countdown.get(Calendar.HOUR) == 0 && countdown.get(Calendar.SECOND) == 0 && countdown.get(
            Calendar.MINUTE
        ) == 0)
    }
}