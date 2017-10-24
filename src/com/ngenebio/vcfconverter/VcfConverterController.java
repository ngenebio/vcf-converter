package com.ngenebio.vcfconverter;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.*;

public class VcfConverterController {
	@FXML
	private Button fileOpenButton;
	
	@FXML
	private ComboBox<String> convertTypeComboBox;
	
	private Stage primaryStage;
	private List<File> vcfFiles;
	
	
	public void show(Stage primaryStage) {
		this.primaryStage = primaryStage;
		fileOpenButton.setOnAction(e -> {
			FileChooser fileChooser = new FileChooser();			
			fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Variant Calling Format(*.vcf)", "*.vcf"));
			fileChooser.setTitle("VCF Files ");
			fileChooser.setInitialDirectory(
	                new File(System.getProperty("user.home"))
	            );
			vcfFiles = fileChooser.showOpenMultipleDialog(primaryStage);
						
		});
		convertTypeComboBox.getItems().add("EXCEL");
		convertTypeComboBox.getSelectionModel().selectFirst();
	}



	@FXML
	private void startConvert() {
		Task<List<File>> convertTask = new Task<List<File>>() {

			@Override
			protected List<File> call() throws Exception {
				vcfFiles.stream().forEach(vcfFile -> {
					try(BufferedReader reader = new BufferedReader(new FileReader(vcfFile))) {
						String[] infoHeaders = reader.lines().filter( line -> line.startsWith("##INFO")).map(line -> line.split("=<")[1].splite(",")[0].split("=")[0])
					}
					Workbook wb = new XSSFWorkbook();
				    CreationHelper createHelper = wb.getCreationHelper();
				    Sheet sheet = wb.createSheet("new sheet");

				    // Create a row and put some cells in it. Rows are 0 based.
				    Row row = sheet.createRow(0);

				    // Create a cell and put a date value in it.  The first cell is not styled
				    // as a date.
				    Cell cell = row.createCell(0);
				    cell.setCellValue(new Date());
				   
				    // Write the output to a file
				    FileOutputStream fileOut = new FileOutputStream(vcfFile.);
				    wb.write(fileOut);
				    fileOut.close();
				    
				});				
				return null;
			}
			
		}
	}
}
