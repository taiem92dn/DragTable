package com.example.user.dragtable;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.skydoves.colorpickerpreference.ColorEnvelope;
import com.skydoves.colorpickerpreference.ColorListener;
import com.skydoves.colorpickerpreference.ColorPickerDialog;

import java.util.ArrayList;
import java.util.List;

public class ShadowCardDrag extends Activity {

    private FrameLayout cardParent;
    private List<DragView> mCards = new ArrayList<>();
    private DragView selectedCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shadow_card_drag);

        cardParent = findViewById(R.id.card_parent);

        findViewById(R.id.shape_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewCard();
            }
        });

        findViewById(R.id.shape_remove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeCard();
            }
        });

        findViewById(R.id.shape_select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedCard != null) {
                    selectedCard.changeShape();
                }
            }
        });


        findViewById(R.id.color_select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorPicker();
            }
        });

        cardParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedCard != null) {
                    selectedCard.setSelected(false);
                    selectedCard = null;
                }
            }
        });

        addNewCard();
    }

    private void removeCard() {
        cardParent.removeView(selectedCard);
        selectedCard = null;
    }

    private void showColorPicker() {
        ColorPickerDialog.Builder builder = new ColorPickerDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        builder.setTitle("ColorPicker Dialog");
        builder.setPreferenceName("MyColorPickerDialog");
        builder.setPositiveButton(getString(android.R.string.ok), new ColorListener() {
            @Override
            public void onColorSelected(ColorEnvelope colorEnvelope) {
                if (selectedCard != null) {
                    selectedCard.setShapeColor(colorEnvelope.getColor());
                }
                else {
                    cardParent.setBackgroundColor(colorEnvelope.getColor());
                }
            }
        });
        builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private void addNewCard() {
        final DragView dragView = new DragView(this);
        dragView.setId(mCards.size());
        mCards.add(dragView);
        cardParent.addView(dragView);

        dragView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedCard != null) {
                    selectedCard.setSelected(false);
                }
                selectedCard = mCards.get(dragView.getId());
                selectedCard.setSelected(true);
            }
        });
    }
}
