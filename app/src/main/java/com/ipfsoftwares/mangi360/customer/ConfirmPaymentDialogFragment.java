package com.ipfsoftwares.mangi360.customer;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

public class ConfirmPaymentDialogFragment extends DialogFragment {

	public interface ConfirmPaymentListener {
		void onDialogPositiveClick(DialogFragment dialog);
		void onDialogNegativeClick(DialogFragment dialog);
	}

	ConfirmPaymentListener mListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the NoticeDialogListener so we can send events to the host
			mListener = (ConfirmPaymentListener) activity;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString()
					+ " must implement ConfirmPaymentDialogListener");
		}
	}

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
					mListener.onDialogPositiveClick(ConfirmPaymentDialogFragment.this);
				}
			})
			.setNegativeButton(R.string.cancel_payment, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					mListener.onDialogNegativeClick(ConfirmPaymentDialogFragment.this);
				}
			});

		builder.setTitle("Confirm Payment");
		builder.setMessage("Enter PIN to confirm payment.");

		return builder.create();
    }
}
