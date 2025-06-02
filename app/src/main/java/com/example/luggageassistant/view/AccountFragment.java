package com.example.luggageassistant.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.luggageassistant.R;
import com.example.luggageassistant.utils.InputValidator;
import com.example.luggageassistant.viewmodel.AccountViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AccountFragment extends Fragment {
    private MaterialButton logoutButton, btnChangePassword, btnDeleteAccount;
    private AccountViewModel accountViewModel;
    private TextInputEditText editTextFirstName, editTextLastName, editTextEmail, editTextPhone;
    private TextView btnEditFirstName, btnEditLastName, btnEditEmail, btnEditPhone;
    private TextInputLayout layoutFirstName, layoutLastName, layoutEmail, layoutPhone;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_account, container, false);

        layoutFirstName = view.findViewById(R.id.layoutFirstName);
        layoutLastName = view.findViewById(R.id.layoutLastName);
        layoutEmail = view.findViewById(R.id.layoutEmail);
        layoutPhone = view.findViewById(R.id.layoutPhone);

        editTextFirstName = view.findViewById(R.id.editTextFirstName);
        editTextLastName = view.findViewById(R.id.editTextLastName);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        editTextPhone = view.findViewById(R.id.editTextPhone);

        btnEditFirstName = view.findViewById(R.id.btnEditFirstName);
        btnEditLastName = view.findViewById(R.id.btnEditLastName);
        btnEditEmail = view.findViewById(R.id.btnEditEmail);
        btnEditPhone = view.findViewById(R.id.btnEditPhone);

        logoutButton = view.findViewById(R.id.btn_logout);
        btnChangePassword = view.findViewById(R.id.btn_change_password);
        btnDeleteAccount = view.findViewById(R.id.btn_delete_account);

        accountViewModel = new ViewModelProvider(
                requireActivity(),
                new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication())
        ).get(AccountViewModel.class);

        accountViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                editTextFirstName.setText(user.getFirstName());
                editTextLastName.setText(user.getLastName());
                editTextEmail.setText(user.getEmail());
                editTextPhone.setText(user.getPhoneNo());
            } else {
                Toast.makeText(requireContext(), "Error loading user data", Toast.LENGTH_SHORT).show();
            }
        });

        btnEditFirstName.setOnClickListener(v -> {
            if (editTextFirstName.isEnabled()) {
                // 1. Verifică dacă nu e null/gol
                if (!InputValidator.isFieldNotEmpty(layoutFirstName)) return;

                // 2. Verificare suplimentară specifică
                if (!InputValidator.isNameValid(editTextFirstName.getText().toString())) {
                    layoutFirstName.setError("First name is invalid!");
                    layoutFirstName.setBoxStrokeColor(ContextCompat.getColor(requireContext(), R.color.error));
                    return;
                }

                // 3. Save
                layoutFirstName.setError(null);
                editTextFirstName.setEnabled(false);
                editTextFirstName.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary));
                btnEditFirstName.setText("Edit");
                saveFirstName(editTextFirstName.getText().toString());

            } else {
                editTextFirstName.setEnabled(true);
                editTextFirstName.requestFocus();
                editTextFirstName.setTextColor(ContextCompat.getColor(requireContext(), R.color.tertiary));
                btnEditFirstName.setText("Save");
            }
        });

        btnEditLastName.setOnClickListener(v -> {
            if (editTextLastName.isEnabled()) {
                if (!InputValidator.isFieldNotEmpty(layoutLastName)) return;

                if (!InputValidator.isNameValid(editTextLastName.getText().toString())) {
                    layoutLastName.setError("Last name is invalid!");
                    layoutLastName.setBoxStrokeColor(ContextCompat.getColor(requireContext(), R.color.error));
                    return;
                }

                layoutLastName.setError(null);
                editTextLastName.setEnabled(false);
                editTextLastName.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary));
                btnEditLastName.setText("Edit");
                saveLastName(editTextLastName.getText().toString());

            } else {
                editTextLastName.setEnabled(true);
                editTextLastName.requestFocus();
                editTextLastName.setTextColor(ContextCompat.getColor(requireContext(), R.color.tertiary));
                btnEditLastName.setText("Save");
            }
        });

        btnEditPhone.setOnClickListener(v -> {
            if (editTextPhone.isEnabled()) {
                if (!InputValidator.validateAccountField(requireContext(), layoutPhone, editTextPhone)) return;

                editTextPhone.setEnabled(false);
                editTextPhone.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary));
                btnEditPhone.setText("Edit");
                savePhone(editTextPhone.getText().toString());
            } else {
                editTextPhone.setEnabled(true);
                editTextPhone.requestFocus();
                editTextPhone.setTextColor(ContextCompat.getColor(requireContext(), R.color.tertiary));
                btnEditPhone.setText("Save");
            }
        });

        btnEditEmail.setOnClickListener(v -> {
            if (editTextEmail.isEnabled()) {
                if (!InputValidator.validateAccountField(requireContext(), layoutEmail, editTextEmail)) return;

                editTextEmail.setEnabled(false);
                editTextEmail.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary));
                btnEditEmail.setText("Edit");
                saveEmail(editTextEmail.getText().toString());
            } else {
                editTextEmail.setEnabled(true);
                editTextEmail.requestFocus();
                editTextEmail.setTextColor(ContextCompat.getColor(requireContext(), R.color.tertiary));
                btnEditEmail.setText("Save");
            }
        });


        accountViewModel.getLogoutStatus().observe(getViewLifecycleOwner(), isLoggedOut -> {
            if (isLoggedOut) {
                Intent intent = new Intent(requireActivity(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                requireActivity().finish();
            }
        });

        logoutButton.setOnClickListener(v -> accountViewModel.logoutUser());

        btnChangePassword.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            if (!email.isEmpty()) {
                accountViewModel.sendResetPasswordEmail(email, success -> {
                    if (success) {
                        Toast.makeText(requireContext(), "Password reset email sent. Please check your inbox.", Toast.LENGTH_LONG).show();

                        // Delogare imediată
                        accountViewModel.logoutUser(); // va declanșa LiveData și navigare la LoginActivity
                    } else {
                        Toast.makeText(requireContext(), "Failed to send password reset email.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(requireContext(), "Email is empty", Toast.LENGTH_SHORT).show();
            }
        });

        btnDeleteAccount.setOnClickListener(v -> {
            AlertDialog dialog = new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to permanently delete your account?")
                    .setPositiveButton("Yes", (d, which) -> showPasswordDialogForDeletion())
                    .setNegativeButton("Cancel", null)
                    .create();

            dialog.show();

//            Window window = dialog.getWindow();
//            if (window != null) {
//                GradientDrawable background = new GradientDrawable();
//                background.setCornerRadius(30f);
//                window.setBackgroundDrawable(background);
//            }

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));

        });


        accountViewModel.loadUserData();

        return view;
    }

    private void showPasswordDialogForDeletion() {
        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint("Enter your password");
        input.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
        input.setTextSize(16f);
        input.setPadding(50, 40, 50, 40);
        input.setBackground(null);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(30f); // colțuri rotunjite
//        drawable.setColor(ContextCompat.getColor(requireContext(), R.color.background)); // culoarea de fundal

        // Creează view-ul dialogului cu padding
        LinearLayout wrapper = new LinearLayout(requireContext());
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setPadding(40, 50, 40, 20); // spațiu până la margini
        wrapper.setBackground(drawable);
        wrapper.addView(input);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Password")
                .setMessage("For security, please enter your password to delete your account.")
                .setView(wrapper)
                .setPositiveButton("Confirm", (d, which) -> {
                    String password = input.getText().toString().trim();
                    if (!password.isEmpty()) {
                        accountViewModel.deleteUser(password, success -> {
                            if (success) {
                                Toast.makeText(requireContext(), "Account deleted", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(requireContext(), LoginActivity.class));
                                requireActivity().finish();
                            } else {
                                Toast.makeText(requireContext(), "Incorrect password or failed to delete account", Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        Toast.makeText(requireContext(), "Password cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
        dialog.show();

        // Schimbă culorile butoanelor
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
    }

    private void saveFirstName(String firstName) {
        accountViewModel.updateField("firstName", firstName);
    }
    private void saveLastName(String lastName) {
        accountViewModel.updateField("lastName", lastName);
    }
    private void saveEmail(String email) {
        accountViewModel.updateField("email", email);
    }
    private void savePhone(String phone) {
        accountViewModel.updateField("phoneNo", phone);
    }

    public interface Callback {
        void onComplete(boolean success);
    }

}
