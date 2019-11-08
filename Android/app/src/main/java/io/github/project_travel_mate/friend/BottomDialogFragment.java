package io.github.project_travel_mate.friend;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.project_travel_mate.R;

public class BottomDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    @BindView(R.id.call)
    ImageView call;

    @BindView(R.id.message)
    ImageView message;

    @BindView(R.id.call_friend)
    LinearLayout call_friend;

    @BindView(R.id.message_friend)
    LinearLayout message_friend;


    static String contacts;
    public static BottomDialogFragment getInstance(String contact) {
        contacts = contact;
        return new BottomDialogFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.custom_bottom_sheet, container, false);
        ButterKnife.bind(view);
        call_friend = view.findViewById(R.id.call_friend);
        message_friend = view.findViewById(R.id.message_friend);
        call_friend.setOnClickListener(this);
        message_friend.setOnClickListener(this);
        return view;
    }

    public void sendSMS(String number)
    {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", number, null)));
    }

    public void call(String number){
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null));
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.call_friend){
            call(contacts);
        }
        if(v.getId() == R.id.message_friend){
            sendSMS(contacts);
        }
    }
}