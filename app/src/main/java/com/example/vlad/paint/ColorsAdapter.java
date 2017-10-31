package com.example.vlad.paint;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by vlad on 26/10/17.
 */

public class ColorsAdapter extends RecyclerView.Adapter<ColorsAdapter.ViewHolder> {

    private int[] colors;
    private static int selectedColor;
    private static PaintActivity context;

    public ColorsAdapter(Context context, int[] colors) {
        this.context = (PaintActivity) context;
        this.colors = colors;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final Button button;
        public ViewHolder(Button button) {
            super(button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Button button = (Button) view;
                    ColorDrawable buttonColor = (ColorDrawable) button.getBackground();
                    selectedColor =  buttonColor.getColor();
                    context.setColorPickerIconColor(selectedColor);
                    context.paintView.setColor(selectedColor);


                }
            });
            this.button = button;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Button button = (Button) LayoutInflater.from(parent.getContext()).inflate(R.layout.color_picker_item, parent, false);
        return new ViewHolder(button);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.button.setBackgroundColor(colors[position]);
    }

    @Override
    public int getItemCount() {
        return colors.length;
    }
}
