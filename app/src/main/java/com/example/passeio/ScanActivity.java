package com.example.passeio;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import br.com.edu.unicid.qrcodeteste.R;

public class ScanActivity extends AppCompatActivity {

    private TextView txtNome, txtDataNascimento, txtId;
    private CadastroDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_scanned_person); // Replace with your layout file

        txtNome = findViewById(R.id.txtNome);
        txtDataNascimento = findViewById(R.id.txtDataNascimento);
        txtId = findViewById(R.id.txtId);

        dbHelper = new CadastroDbHelper(this);

        // Initiate QR code scan
        new IntentIntegrator(this).initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Scaneamento cancelado", Toast.LENGTH_LONG).show();
            } else {
                // QR code scanned successfully
                String qrCodeData = result.getContents();
                processQrCodeData(qrCodeData);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void processQrCodeData(String qrCodeData) {
        try {
            // 1. Split the QR code data using the "|" delimiter
            String[] parts = qrCodeData.split("\\|");// 2. Check if the data has the expected format (three parts: ID, name, date of birth)
            if (parts.length == 3) {
                String idString = parts[0]; // Extract ID as String
                String nome = parts[1];
                String dataNascimento = parts[2];

                // 3. Query the database using the extracted ID
                Cursor cursor = dbHelper.getRegistroById(Long.parseLong(idString)); // Query by ID

                // 4. Check if a matching record is found
                if (cursor != null&& cursor.moveToFirst()) {
                    // 5. If found, display the ID, name, and date of birth
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(CadastroDbHelper.COLUMN_ID));
                    nome = cursor.getString(cursor.getColumnIndexOrThrow(CadastroDbHelper.COLUMN_NOME));
                    dataNascimento = cursor.getString(cursor.getColumnIndexOrThrow(CadastroDbHelper.COLUMN_DATA_NASCIMENTO));

                    txtNome.setText("Nome: " + nome);
                    txtDataNascimento.setText("Data de Nascimento: " + dataNascimento);
                    // Add TextView for ID in your layout (e.g., txtId)
                    txtId.setText("ID: " + id); // Display ID

                    // 6. Close the cursor
                    cursor.close();
                } else {
                    // 7. If not found, display "Registro não encontrado"
                    Toast.makeText(this, "Registro não encontrado", Toast.LENGTH_SHORT).show();
                }
            } else {
                // 8. If the QR code data format is invalid, display "Invalid QR Code format"
                Toast.makeText(this, "Invalid QRCode format", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            // 9. Handle any unexpected exceptions
            Toast.makeText(this, "Error processing QR Code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("ScanActivity", "Error processing QR Code", e);
        }
    }
}
