package com.swaraj.todolist;

import com.swaraj.todolist.dataModel.ToDoItem;
import com.swaraj.todolist.services.DatabaseService;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class Controller {
    private ObservableList<ToDoItem> toDoItems;
    private DatabaseService databaseService;
    @FXML
    private ListView<ToDoItem> todoListView;
    @FXML
    private TextArea itemDetailsTextArea;
    @FXML
    private Label deadlinelabel;
    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private ContextMenu listContextMenu;
    @FXML
    private ToggleButton filterToggleButton;
    private FilteredList<ToDoItem> filteredList;
    private Predicate<ToDoItem> wantAllItems;
    private Predicate<ToDoItem> wantsTodaysItems;

    public void initialize(){
        databaseService = DatabaseService.getInstance();
        loadToDoItems();
        
        listContextMenu = new ContextMenu();
        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ToDoItem item = todoListView.getSelectionModel().getSelectedItem();
                deleteItem(item);
            }
        });

        listContextMenu.getItems().addAll(deleteMenuItem);  //This line is important to load the new window or the functionality.
        todoListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ToDoItem>() {
            @Override
            public void changed(ObservableValue<? extends ToDoItem> observableValue, ToDoItem toDoItem, ToDoItem t1) {
                if(t1 != null){
                    ToDoItem item = todoListView.getSelectionModel().getSelectedItem();
                    itemDetailsTextArea.setText(item.getDetails());
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("MMMM d, yyyy");
                    deadlinelabel.setText(df.format(item.getDeadline()));
                }
            }
        });
        wantAllItems = new Predicate<ToDoItem>() {
            @Override
            public boolean test(ToDoItem item) {
                return true;
            }
        };
        wantsTodaysItems = new Predicate<ToDoItem>() {
            @Override
            public boolean test(ToDoItem item) {
                return (item.getDeadline().toLocalDate().equals(LocalDate.now()));
            }
        };

        filteredList = new FilteredList<ToDoItem>(toDoItems, wantAllItems);

        SortedList<ToDoItem> sortedList = new SortedList<ToDoItem>(filteredList, new Comparator<ToDoItem>() {
            @Override
            public int compare(ToDoItem o1, ToDoItem o2) {
                return o1.getDeadline().compareTo(o2.getDeadline());
            }
        });

//        todoListView.setItems(ToDoData.getInstance().getToDoItems());
        todoListView.setItems(sortedList);
        todoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        todoListView.getSelectionModel().selectFirst();
        todoListView.setCellFactory(new Callback<ListView<ToDoItem>, ListCell<ToDoItem>>() {
            @Override
            public ListCell<ToDoItem> call(ListView<ToDoItem> toDoItemListView) {
                ListCell<ToDoItem> cell = new ListCell<ToDoItem>(){
                    @Override
                    protected void updateItem(ToDoItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty){
                            setText(null);
                        }else {
                            setText(item.getShortDescription());
                            if(item.getDeadline().toLocalDate().isBefore(LocalDate.now().plusDays(1))){
                                setTextFill(Color.RED);
                            } else if (item.getDeadline().toLocalDate().equals(LocalDate.now().plusDays(1))) {
                                setTextFill(Color.ORANGE);
                            }
                        }
                    }
                };
                cell.emptyProperty().addListener(
                        (obs, wasEmpty, isNowEmpty) -> {
                            if(isNowEmpty) {
                                cell.setContextMenu(null);
                            } else {
                                cell.setContextMenu(listContextMenu);
                            }

                        });
                return cell;
            }
        });
    }


    @FXML
    public void showNewItemDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Add New ToDo Item");
        dialog.setHeaderText("This is where you can add new todo item.");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("ToDoItemDialog.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());

        } catch (IOException e) {
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            DialogController controller = fxmlLoader.getController();
            ToDoItem newItem = controller.processResults();
            if (newItem != null) {
                // Refresh the list to show the new item
                loadToDoItems();
                filteredList = new FilteredList<ToDoItem>(toDoItems, 
                    filterToggleButton.isSelected() ? wantsTodaysItems : wantAllItems);
                
                SortedList<ToDoItem> sortedList = new SortedList<ToDoItem>(filteredList, new Comparator<ToDoItem>() {
                    @Override
                    public int compare(ToDoItem o1, ToDoItem o2) {
                        return o1.getDeadline().compareTo(o2.getDeadline());
                    }
                });
                todoListView.setItems(sortedList);
                todoListView.getSelectionModel().select(newItem);
            }
        }
    }
    @FXML
        public void handleClickListView(){
        ToDoItem item = todoListView.getSelectionModel().getSelectedItem();
        itemDetailsTextArea.setText(item.getDetails());
        deadlinelabel.setText(item.getDeadline().toString());
     }
    @FXML
    public void handleKeyPressed(KeyEvent keyEvent) {
        ToDoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
        if(selectedItem != null){
            if(keyEvent.getCode().equals(KeyCode.DELETE)){
                deleteItem(selectedItem);
            }
        }
    }

     private void deleteItem( ToDoItem item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete a ToDo item");
        alert.setHeaderText("Delete item:" + item.getShortDescription());
        alert.setContentText("Are you sure, press ok to delete and cancel to back out");
        Optional<ButtonType> result = alert.showAndWait();

        if(result.isPresent() && (result.get()==ButtonType.OK)){
            databaseService.deleteTodoItem(item.getId());
            loadToDoItems(); // Refresh the list
            filteredList = new FilteredList<ToDoItem>(toDoItems, 
                filterToggleButton.isSelected() ? wantsTodaysItems : wantAllItems);
            
            SortedList<ToDoItem> sortedList = new SortedList<ToDoItem>(filteredList, new Comparator<ToDoItem>() {
                @Override
                public int compare(ToDoItem o1, ToDoItem o2) {
                    return o1.getDeadline().compareTo(o2.getDeadline());
                }
            });
            todoListView.setItems(sortedList);
            todoListView.getSelectionModel().selectFirst();
        }
    }

    @FXML
    public void handleFilterButton(ActionEvent event) {
        ToDoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
        if (filterToggleButton.isSelected()){
            filteredList.setPredicate(wantsTodaysItems);
            if(filteredList.isEmpty()){
                itemDetailsTextArea.clear();
                deadlinelabel.setText("");
            }else if(filteredList.contains(selectedItem)){
                todoListView.getSelectionModel().select(selectedItem);
            }else{
                todoListView.getSelectionModel().selectFirst();
            }
        }else {
            filteredList.setPredicate(wantAllItems);
        }
    }

    public void handleExit(ActionEvent event) {
        Platform.exit();
    }

    private void loadToDoItems() {
        toDoItems = databaseService.loadTodoItems();
    }

    // Missing FXML action methods - implementing stubs for now
    @FXML
    public void importTasks(ActionEvent event) {
        // TODO: Implement import functionality
        showNotImplementedAlert("Import Tasks");
    }
    
    @FXML
    public void exportTasks(ActionEvent event) {
        // TODO: Implement export functionality
        showNotImplementedAlert("Export Tasks");
    }
    
    @FXML
    public void createBackup(ActionEvent event) {
        // TODO: Implement backup functionality
        showNotImplementedAlert("Create Backup");
    }
    
    @FXML
    public void editSelectedItem(ActionEvent event) {
        // TODO: Implement edit functionality
        showNotImplementedAlert("Edit Selected Item");
    }
    
    @FXML
    public void deleteSelectedItem(ActionEvent event) {
        ToDoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
        if(selectedItem != null) {
            deleteItem(selectedItem);
        }
    }
    
    @FXML
    public void duplicateSelectedItem(ActionEvent event) {
        // TODO: Implement duplicate functionality
        showNotImplementedAlert("Duplicate Selected Item");
    }
    
    @FXML
    public void toggleCompleteSelectedItem(ActionEvent event) {
        // TODO: Implement toggle complete functionality
        showNotImplementedAlert("Toggle Complete");
    }
    
    @FXML
    public void refreshAll(ActionEvent event) {
        loadToDoItems();
        filteredList = new FilteredList<ToDoItem>(toDoItems, 
            filterToggleButton.isSelected() ? wantsTodaysItems : wantAllItems);
        
        SortedList<ToDoItem> sortedList = new SortedList<ToDoItem>(filteredList, new Comparator<ToDoItem>() {
            @Override
            public int compare(ToDoItem o1, ToDoItem o2) {
                return o1.getDeadline().compareTo(o2.getDeadline());
            }
        });
        todoListView.setItems(sortedList);
        todoListView.getSelectionModel().selectFirst();
    }
    
    @FXML
    public void clearFilters(ActionEvent event) {
        if (filterToggleButton != null) {
            filterToggleButton.setSelected(false);
        }
        if (filteredList != null) {
            filteredList.setPredicate(wantAllItems);
        }
    }
    
    @FXML
    public void toggleStatistics(ActionEvent event) {
        // TODO: Implement statistics toggle
        showNotImplementedAlert("Toggle Statistics");
    }
    
    @FXML
    public void toggleTheme(ActionEvent event) {
        // TODO: Implement theme toggle
        showNotImplementedAlert("Toggle Theme");
    }
    
    private void showNotImplementedAlert(String feature) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Feature Not Implemented");
        alert.setHeaderText(feature);
        alert.setContentText("This feature will be implemented in a future version.");
        alert.showAndWait();
    }
}
