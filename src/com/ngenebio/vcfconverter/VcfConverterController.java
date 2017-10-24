package com.ngenebio.vcfconverter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class VcfConverterController {
	@FXML
	private Button fileOpenButton;
	
	@FXML
	private ComboBox<String> convertTypeComboBox;
	
	private List<File> vcfFiles;
	
	
	public void show(Stage primaryStage) {
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
				List<File> outputFiles = new ArrayList<File>();
				vcfFiles.stream().forEach(vcfFile -> {				
				   
					try(BufferedReader reader = new BufferedReader(new FileReader(vcfFile))) {
						
						Map<String, Integer> infoHeaderMap = new HashMap<>();
						Map<String, Integer> formatHeaderMap = new HashMap<>();
						String[] normalHeaders;
						int colIndex = 0;
						int rowIndex = 0;
						
						Workbook wb = new XSSFWorkbook();
					    CreationHelper createHelper = wb.getCreationHelper();
					    Sheet sheet = wb.createSheet("new sheet");
		
					    // Create a row and put some cells in it. Rows are 0 based.
					    Row row = null;	
					    Cell cell = null; 
					    
						while (true) {
							String line = reader.readLine();
							if (line == null || line.length() == 0) {
								break;
							}
							int infoColIndex = 0;
							int formatColIndex = 0;
							if (line.startsWith("##INFO")){
								String id = line.split("=<")[1].split(",")[0].split("=")[1];
								infoHeaderMap.put(id, infoColIndex);
								infoColIndex += 1;
							}
							if (line.startsWith("##FORMAT")){
								String id = line.split("=<")[1].split(",")[0].split("=")[1];
								formatHeaderMap.put(id, formatColIndex);
								formatColIndex += 1;
							}
							if (line.startsWith("#CHROM")){
								normalHeaders = line.split("\t");
								row = sheet.createRow(rowIndex);								
								for(String h : normalHeaders) {
									cell = row.createCell(colIndex);
									cell.setCellValue(h);
									colIndex += 1;
								}
								rowIndex += 1;
							}
							if (line.startsWith("chr")) {
								row = sheet.createRow(rowIndex);
								colIndex = 0;
								String[] items = line.split("\t");
								for(String i : items) {
									cell = row.createCell(colIndex);
									cell.setCellValue(i);
									colIndex += 1;
								}
								
							}							
						}
						File outFile = new File(vcfFile.getName().replace(".vcf", ".xslx"));
						// Write the output to a file
					    FileOutputStream fileOut = new FileOutputStream(outFile);
					    wb.write(fileOut);
					    wb.close();
					    fileOut.close();
					    outputFiles.add(outFile);
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			
				return outputFiles;
			}
			
		};
		Thread t = new Thread(convertTask);
		t.start();
	}
}
