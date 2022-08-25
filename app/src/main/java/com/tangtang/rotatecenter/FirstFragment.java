package com.tangtang.rotatecenter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

public class FirstFragment extends Fragment {

    private ViewPager2 viewPager;
    private int position;
    private AnimatorAdapter adapter;
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                NavHostFragment.findNavController(FirstFragment.this)
//                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
                position ++;
                if (position >= adapter.getItemCount()){
                    position = 0;
                }
                viewPager.setCurrentItem(position, true);
            }
        });
        viewPager = view.findViewById(R.id.view_page);
        initTransform();
    }

    private void initTransform(){
        RotateCenterTransform transform = new RotateCenterTransform();
        transform.setDuration(2000);
        transform.setOrientation(RecyclerView.HORIZONTAL);
//        transform.setOrientation(RecyclerView.VERTICAL);
        viewPager.setPageTransformer(transform);
        adapter = new AnimatorAdapter(5, transform);
        viewPager.setAdapter(adapter);
    }
}