package com.example.calculatorapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // Поля введення
    private EditText etFirstNumber;
    private EditText etSecondNumber;

    // Група перемикачів вибору операції
    private RadioButton rbAdd;
    private RadioButton rbSubtract;
    private RadioButton rbMultiply;
    private RadioButton rbDivide;

    // Прапорець та перемикач
    private CheckBox cbDetails;
    private ToggleButton tbSound;

    // Повзунок точності округлення
    private SeekBar seekBarPrecision;
    private TextView tvPrecisionLabel;

    //  Дата та час
    private TextView tvSelectedDate;
    private TextView tvSelectedTime;

    // Заголовок та результат
    private TextView tvResult;
    private ListView lvHistory;
    private ArrayList<String> historyList;
    private ArrayAdapter<String> adapter;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Назва файлу налаштувань і режим доступу (MODE_PRIVATE робить файл доступним лише вашому додатку)
        sharedPreferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        historyList = new ArrayList<String>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, historyList);
        bindViews();
        setupSeekBar();
        setupToggleButton();
        setupDatePicker();
        setupTimePicker();
        setupCalculateButton();
        setupClearButton();

        boolean tbBtn = sharedPreferences.getBoolean("SOUND_BTN", false);
        tbSound.setChecked(tbBtn);

        int skBtn = sharedPreferences.getInt("KEY_ACCURACY", 0);
        seekBarPrecision.setProgress(skBtn);
    }

    private void bindViews() {
        etFirstNumber = findViewById(R.id.etFirstNumber);
        etSecondNumber = findViewById(R.id.etSecondNumber);

        rbAdd = findViewById(R.id.rbAdd);
        rbSubtract = findViewById(R.id.rbSubtract);
        rbMultiply = findViewById(R.id.rbMultiply);
        rbDivide = findViewById(R.id.rbDivide);

        cbDetails = findViewById(R.id.cbDetails);
        tbSound = findViewById(R.id.tbSound);

        seekBarPrecision = findViewById(R.id.seekBarPrecision);
        tvPrecisionLabel = findViewById(R.id.tvPrecisionLabel);

        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvSelectedTime = findViewById(R.id.tvSelectedTime);

        tvResult = findViewById(R.id.tvResult);
        lvHistory = findViewById(R.id.lvHistory);
        lvHistory.setNestedScrollingEnabled(true);
        lvHistory.setAdapter(adapter);
    }

    // Обробка зміни положення повзунка SeekBar
    private void setupSeekBar() {
        seekBarPrecision.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvPrecisionLabel.setText(
                        "Точність округлення " + progress + " знаків після коми");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Користувач відпустив повзунок — зберігаємо фінальне значення
                int finalValue = seekBar.getProgress();

                sharedPreferences.edit()
                        .putInt("KEY_ACCURACY", finalValue)
                        .apply(); // Обов'язково викликаємо apply()
            }
        });


    }

    // Обробка перемикання ToggleButton
    private void setupToggleButton() {
        tbSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String message = isChecked
                    ? "Звукові сповіщення активовано"
                    : "Звукові сповіщення деактивовано";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            sharedPreferences.edit()
                    .putBoolean("SOUND_BTN", isChecked)
                    .apply();
        });
    }

    // Виклик діалогового вікна вибору дати
    private void setupDatePicker() {
        Button btnDate = findViewById(R.id.btnDate);
        btnDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(
                    MainActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        String date = String.format(Locale.getDefault(),
                                "%02d.%02d.%d", dayOfMonth, month + 1, year);
                        tvSelectedDate.setText("Дата " + date);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)

            );
            dialog.show();
        });
    }

    // Виклик діалогового вікна вибору часу
    private void setupTimePicker() {
        Button btnTime = findViewById(R.id.btnTime);
        btnTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            TimePickerDialog dialog = new TimePickerDialog(
                    MainActivity.this,
                    (view, hourOfDay, minute) -> {
                        String time = String.format(Locale.getDefault(),
                                "%02d.%02d", hourOfDay, minute);
                        tvSelectedTime.setText("Час " + time);
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
            );
            dialog.show();
        });
    }

    // Обробка кнопки обчислення та логіка калькулятора
    private void setupCalculateButton() {
        Button btnCalculate = findViewById(R.id.btnCalculate);
        btnCalculate.setOnClickListener(v -> performCalculation());
    }

    private void performCalculation() {
        try {
            double a = Double.parseDouble(etFirstNumber.getText().toString());
            double b = Double.parseDouble(etSecondNumber.getText().toString());

            if(a<-1000 || a>1000 ) {
                etFirstNumber.setError("The first number is out of diapazone");
                return;
            }
            else if(b<-1000 || b>1000 ) {
                etSecondNumber.setError("The second number is out of diapazone");
                return;
            }


            double result;
            String operationName;
            String operationSymbol;

            if (rbAdd.isChecked()) {
                result = a + b;
                operationName = "додавання";
                operationSymbol = "+";
            } else if (rbSubtract.isChecked()) {
                result = a - b;
                operationName = "віднімання";
                operationSymbol = "-";
            } else if (rbMultiply.isChecked()) {
                result = a * b;
                operationName = "множення";
                operationSymbol = "*";
            } else {
                if (b == 0) {
                    Toast.makeText(this, "Ділення на нуль неможливе",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                result = a / b;
                operationName = "ділення";
                operationSymbol = "/";
            }

            String formatted = String.format(Locale.getDefault(),
                    "%." + seekBarPrecision.getProgress() + "f", result);

            // Врахування стану прапорця CheckBox
            if (cbDetails.isChecked()) {
                tvResult.setText(String.format(Locale.getDefault(),
                        "Результат %s отримано операцією %s зі значень %s та %s",
                        formatted, operationSymbol, a, b));
            } else {
                tvResult.setText("Результат " + formatted);
            }

            // Спливаюче повідомлення Toast
            Toast.makeText(this, "Обчислення виконано успішно",
                    Toast.LENGTH_SHORT).show();

            makeCalculationRecord(a, operationSymbol, b, formatted);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Перевірте правильність введених чисел",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // Обробка кнопки очищення форми та панель Snackbar
    private void setupClearButton() {
        Button btnClear = findViewById(R.id.btnClear);
        btnClear.setOnClickListener(v -> {
            final String previousText = tvResult.getText().toString();

            etFirstNumber.setText("");
            etSecondNumber.setText("");
            tvResult.setText("Результат з'явиться тут");
            tvSelectedDate.setText("Дата не обрана");
            tvSelectedTime.setText("Час не обраний");

            Snackbar.make(v, "Форму очищено", Snackbar.LENGTH_LONG)
                    .setAction("Скасувати", view -> tvResult.setText(previousText))
                    .show();
        });
    }

    private void makeCalculationRecord(double a, String op, double b, String res) {
        String dateText = tvSelectedDate.getText().toString();
        String timeText = tvSelectedTime.getText().toString();

        // Перевіряємо, чи користувач обрав дату і час (перевірка на дефолтні значення)
        if (dateText.equals("Дата не обрана") || timeText.equals("Час не обраний") || dateText.isEmpty() || timeText.isEmpty()) {
            Toast.makeText(this, "Встановіть дату і час", Toast.LENGTH_SHORT).show();
            return;
        }

        // Формуємо читабельний запис з отриманих значень
        String record = String.format(Locale.getDefault(), "[%s | %s]  %.2f %s %.2f = %s",
                dateText, timeText, a, op, b, res);

        // Додаємо новий запис на початок списку (індекс 0) і оновлюємо адаптер
        historyList.add(0, record);
        adapter.notifyDataSetChanged();
    }
}
