package com.example.luggageassistant.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class MultiSelectDialog {

    public static void showCustomMultiSelectDialog(Context context,
                                                   String title,
                                                   String[] options,
                                                   boolean[] checkedItems,
                                                   List<String> selectedItems,
                                                   TextView outputTextView) {

        LinearLayout dialogLayout = new LinearLayout(context);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(50, 20, 50, 0);

        EditText otherInput = new EditText(context);
        otherInput.setHint("Add custom option...");
        otherInput.setInputType(InputType.TYPE_CLASS_TEXT);
        dialogLayout.addView(otherInput);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMultiChoiceItems(options, checkedItems, (dialog, which, isChecked) -> {
            if (isChecked) {
                if (!selectedItems.contains(options[which])) {
                    selectedItems.add(options[which]);
                }
            } else {
                selectedItems.remove(options[which]);
            }
        });

        builder.setView(dialogLayout);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String other = otherInput.getText().toString().trim();
            if (!other.isEmpty() && !selectedItems.contains(other)) {
                selectedItems.add(other);
            }

            if (selectedItems.isEmpty()) {
                outputTextView.setText("Nothing selected");
            } else {
                outputTextView.setText(String.join(", ", selectedItems));
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }
}
