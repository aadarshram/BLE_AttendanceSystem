// MainActivity.kt
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var scanSignal: ScanSignal
    private lateinit var resultTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val scanButton: Button = findViewById(R.id.scanButton)
        resultTextView = findViewById(R.id.resultTextView)

        scanSignal =
                ScanSignal(this) { success ->
                    resultTextView.text =
                            if (success) "Scan succeeded" else "Scan failed or timed out"
                }

        scanButton.setOnClickListener {
            resultTextView.text = "Scanning..."
            scanSignal.startScan()
        }
    }
}
