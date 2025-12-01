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
    private final ObservableList<T> originalItems;
    private String filter;

    public ComboBoxAutoComplete(ComboBox<T> cmb) {
        this.cmb = cmb;
        this.originalItems = FXCollections.observableArrayList(cmb.getItems());
        this.cmb.setTooltip(new Tooltip());
        this.cmb.setOnKeyPressed(this::handleOnKeyPressed);
        this.cmb.setOnHidden(this::handleOnHiding);
        this.filter = "";
    }

    public void handleOnKeyPressed(KeyEvent e) {
        ObservableList<T> filteredList = FXCollections.observableArrayList();
        KeyCode code = e.getCode();
        if (code.isLetterKey()) {
            filter = filter + e.getText();
        }

        if (code == KeyCode.BACK_SPACE && !filter.isEmpty()) {
            filter = filter.substring(0, filter.length() - 1);
            cmb.getItems().setAll(originalItems);
        }

        if (code == KeyCode.ESCAPE) {
            filter = "";
        }

        if (filter.isEmpty()) {
            filteredList = originalItems;
            cmb.getTooltip().hide();
        } else {
            Stream<T> itens = cmb.getItems().stream();
            String txtUsr = filter.toLowerCase();
            itens.filter(el -> el.toString().toLowerCase().contains(txtUsr)).forEach(filteredList::add);
            cmb.getTooltip().setText(txtUsr);
            Window stage = cmb.getScene().getWindow();
            double posX = stage.getX() + cmb.localToScene(cmb.getBoundsInLocal()).getMinX();
            double posY = stage.getY() + cmb.localToScene(cmb.getBoundsInLocal()).getMinY();
            cmb.getTooltip().show(stage, posX, posY);
            cmb.show();
        }

        cmb.getItems().setAll(filteredList);
    }

    public void handleOnHiding(Event e) {
        filter = "";
        cmb.getTooltip().hide();
        T s = cmb.getSelectionModel().getSelectedItem();
        cmb.getItems().setAll(originalItems);
        cmb.getSelectionModel().select(s);
    }
}
