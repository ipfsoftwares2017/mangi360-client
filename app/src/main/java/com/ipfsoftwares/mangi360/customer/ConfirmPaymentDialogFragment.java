package com.ipfsoftwares.mangi360.customer;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import android.support.v4.app.DialogFragment;

import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.Toast;

public class ConfirmPaymentDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		builder.setView(inflater.inflate(R.layout.dialog_confirm_payment, null))
			// Add action buttons
			.setPositiveButton(R.string.confirm_payment, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					Toast.makeText(ConfirmPaymentDialogFragment.this.getContext(), "Payment confirmed!", Toast.LENGTH_SHORT).show();
				}
			})
			.setNegativeButton(R.string.cancel_payment, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					ConfirmPaymentDialogFragment.this.getDialog().cancel();
				}
			});

		builder.setTitle("Confirm Payment");
		builder.setMessage("Enter PIN to confirm payment.");
		return builder.create();
    }
}
