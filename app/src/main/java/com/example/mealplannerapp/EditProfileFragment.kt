package com.example.mealplannerapp

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.mealplannerapp.databinding.FragmentEditProfileBinding

class EditProfileFragment : BaseFragment<FragmentEditProfileBinding>(FragmentEditProfileBinding::inflate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editProfileImageButton.setOnClickListener {
            showBottomDialogChangeUsername()
        }
        binding.changePasswordImageButton.setOnClickListener {
            showBottomDialogChangePassword()
        }
        binding.imageButtonBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        binding.deleteImageButton.setOnClickListener {
            showAccountDeletionDialog()
        }
    }

    //TODO:implement database connections to reflect username/password modifications
    private fun showBottomDialogChangeUsername() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_dialog_change_username)

        val buttonConfirm = dialog.findViewById<Button>(R.id.buttonChangeUsername)
        val cancelButton = dialog.findViewById<ImageButton>(R.id.cancelButton)

        buttonConfirm.setOnClickListener {
            val newUsername = dialog.findViewById<EditText>(R.id.newUsernameEditText).text.toString().trim()
            if (newUsername.isEmpty()) {
                Toast.makeText(requireContext(), "Username cannot be empty", Toast.LENGTH_SHORT).show()
            } else {
                SharedPreferencesManager.saveUsername(requireContext(), newUsername)
                Toast.makeText(requireContext(), "Username successfully changed", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
    }

    private fun showBottomDialogChangePassword() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_dialog_change_password)

        val savedPassword = SharedPreferencesManager.getPassword(requireContext())
        val buttonConfirm = dialog.findViewById<Button>(R.id.buttonChangePassword)
        val cancelButton = dialog.findViewById<ImageButton>(R.id.cancelButton)

        buttonConfirm.setOnClickListener {
            val oldPassword = dialog.findViewById<EditText>(R.id.oldPasswordEditText).text.toString().trim()
            val newPassword = dialog.findViewById<EditText>(R.id.newPasswordEditText).text.toString().trim()
            val repeatPassword = dialog.findViewById<EditText>(R.id.repeatNewPasswordEditText).text.toString().trim()
            if (oldPassword.isEmpty() || newPassword.isEmpty() || repeatPassword.isEmpty()) {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
            } else if (oldPassword != savedPassword) {
                Toast.makeText(requireContext(), "Current password is incorrect", Toast.LENGTH_SHORT).show()
            } else if (newPassword != repeatPassword) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                SharedPreferencesManager.savePassword(requireContext(), newPassword)
                Toast.makeText(requireContext(), "Password successfully changed", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
    }

    private fun showAccountDeletionDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Account")
        builder.setMessage("Are you sure you want to delete your account? All your data will be removed.")
        builder.setPositiveButton("Delete") { _, _ ->
            SharedPreferencesManager.clearUserCredentials(requireContext())
            //TODO: Implement logic to delete account

            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        builder.setNegativeButton("Cancel", null)

        val dialog = builder.create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
            ContextCompat.getColor(requireContext(), R.color.reddishOrange)
        )
    }

}