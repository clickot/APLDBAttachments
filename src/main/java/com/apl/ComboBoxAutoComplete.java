package com.apl;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;

import java.util.stream.Stream;

public class ComboBoxAutoComplete<T> {
    private final ComboBox<T> cmb;
    String filter = "";
    private final ObservableList<T> originalItems;

    public ComboBoxAutoComplete(ComboBox<T> cmb) {
        this.cmb = cmb;
        this.originalItems = FXCollections.observableArrayList(cmb.getItems());
        cmb.setTooltip(new Tooltip());
        cmb.setOnKeyPressed(this::handleOnKeyPressed);
        cmb.setOnHidden(this::handleOnHiding);
    }

    public void handleOnKeyPressed(KeyEvent e) {
        ObservableList<T> filteredList = FXCollections.observableArrayList();
        KeyCode code = e.getCode();
        if (code.isLetterKey()) {
            this.filter = this.filter + e.getText();
        }

        if (code == KeyCode.BACK_SPACE && this.filter.length() > 0) {
            this.filter = this.filter.substring(0, this.filter.length() - 1);
            this.cmb.getItems().setAll(this.originalItems);
        }

        if (code == KeyCode.ESCAPE) {
            this.filter = "";
        }

        if (this.filter.length() == 0) {
            filteredList = this.originalItems;
            this.cmb.getTooltip().hide();
        } else {
            Stream<T> itens = this.cmb.getItems().stream();
            String txtUsr = this.filter.toLowerCase();
            itens.filter(el -> el.toString().toLowerCase().contains(txtUsr)).forEach(filteredList::add);
            this.cmb.getTooltip().setText(txtUsr);
            Window stage = this.cmb.getScene().getWindow();
            double posX = stage.getX() + this.cmb.localToScene(this.cmb.getBoundsInLocal()).getMinX();
            double posY = stage.getY() + this.cmb.localToScene(this.cmb.getBoundsInLocal()).getMinY();
            this.cmb.getTooltip().show(stage, posX, posY);
            this.cmb.show();
        }

        this.cmb.getItems().setAll(filteredList);
    }

    public void handleOnHiding(Event e) {
        this.filter = "";
        this.cmb.getTooltip().hide();
        T s = this.cmb.getSelectionModel().getSelectedItem();
        this.cmb.getItems().setAll(this.originalItems);
        this.cmb.getSelectionModel().select(s);
    }
}
