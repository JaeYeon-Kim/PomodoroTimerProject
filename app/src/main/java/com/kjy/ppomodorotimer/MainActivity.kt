package com.kjy.ppomodorotimer

import android.annotation.SuppressLint
import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.SeekBar
import android.widget.TextView

class MainActivity : AppCompatActivity() {


    // 변수 설정.
    private val remainMinutesTextView: TextView by lazy {
        findViewById(R.id.remainMinuteTextView)
    }

    private val remainSecondTextView: TextView by lazy {
        findViewById(R.id.remainSecondsTextView)
    }
    private val seekBar: SeekBar by lazy {
        findViewById(R.id.seekBar)
    }

    // 빌더 패턴으로 사운드풀 선언
    private val soundPool = SoundPool.Builder().build()

    //
    private var currentCountDownTimer: CountDownTimer? = null
    private var tickingSoundId: Int? = null
    private var bellSoundId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()

        initSounds()
    }

    // 다시 앱을 불러올시 시작
    override fun onResume() {
        super.onResume()
        soundPool.autoResume()
    }

    // 앱이 화면에서 보이지 않을 경우 일시 정지
    override fun onPause() {
        super.onPause()
        // autoPouse로 모든 스트리밍된것을 Pause하게됨.
        soundPool.autoPause()
    }

    // 더이상 사용하지 않을때 soundPool 해제
    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }

    private fun bindViews() {
        seekBar.setOnSeekBarChangeListener(
            // SeekBar에 대한 콜백 3개 설정.
            // object: 객체가 하나만 필요해서 사용하는 경우에 쓰는 키워드: 싱글톤 디자인패턴
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    // 프로그래스바 이동에 따른 텍스트뷰 변경
                    // 분 초 단위로 숫자가 변경되도록 하기 위해 수정
                    // 초가 00초에 시작되지않고 58 or 57초에 되는 현상을 없애기 위해 사용자가 건드렸을 경우에만 동작하도록 변경해줌.
                    if(fromUser) {
                        updateRemainTime(progress * 60 * 1000L)
                    }


                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                   stopCountDown()

                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {

                    seekBar ?: return

                    if(seekBar.progress == 0) {
                        stopCountDown()
                    } else {
                        startCountDown()
                    }

                }

            }
        )
    }

    private fun initSounds() {
        tickingSoundId = soundPool.load(this, R.raw.timer_ticking, 1)
        bellSoundId = soundPool.load(this, R.raw.timer_bell, 1)

    }

    private fun createCountDownTimer(initialMillis: Long) =
        object: CountDownTimer(initialMillis, 1000L) {
            // 1초마다 갱신됌.
            override fun onTick(millisUntilFinished: Long) {
                updateRemainTime(millisUntilFinished)
                updateSeekBar(millisUntilFinished)

            }

            override fun onFinish() {
                completeCountDown()

            }
        }

    private fun startCountDown() {

        currentCountDownTimer = createCountDownTimer(seekBar.progress * 60 * 1000L)

        // 언제라도 nullable 할 수 있어서 ? 호출
        currentCountDownTimer?.start()

        tickingSoundId?.let { soundId ->
            // 카운트다운타이머가 시작됨과 동시에 사운드풀 구현
            // 파라미터: 좌우 볼륨 구현,
            soundPool.play(soundId, 1F, 1F, 0, -1, 1F)
        }

    }

    private fun stopCountDown() {
        currentCountDownTimer?.cancel()
        currentCountDownTimer = null
        soundPool.autoPause()
    }

    private fun completeCountDown() {

        updateRemainTime(0)
        updateSeekBar(0)

        // 기존의 ticking 사운드를 끄고 벨소리를 시작
        soundPool.autoPause()
        bellSoundId?.let { soundId ->
            soundPool.play(soundId, 1F, 1F, 0, 0, 1F)

        }
    }
        @SuppressLint("SetTextI18n")
        private fun updateRemainTime(remainMillis: Long) {
            val remainSeconds = remainMillis / 1000
            remainMinutesTextView.text = "%02d'".format(remainSeconds / 60)
            remainSecondTextView.text = "%02d".format(remainSeconds % 60)       // 60초로 나누어주어 진짜 초를 구해줌.
        }

        private fun updateSeekBar(remainMillis: Long) {
            // Long타입을 int형으로 나누기 때문에 Long타입으로 나오게 되므로 Int형으로 converting
            seekBar.progress = (remainMillis / 1000 / 60).toInt()

    }
}