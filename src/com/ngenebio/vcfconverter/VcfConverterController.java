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
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class VcfConverterController {
	@FXML
	private Button fileOpenButton;
	
	@FXML
	private ComboBox<String> convertTypeComboBox;
	
	@FXML
	private ProgressBar convertProgressBar;
	
	@FXML
	private Label convertStatusLabel;
	
	private List<File> vcfFiles;
	
	private Stage primaryStage;
	
	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}
	public void show() {
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
				this.updateProgress(0, outputFiles.size());
				this.updateMessage("Start");
				int completeCount = 0;
				String completeMessage = "Complete. \n";
				for(File vcfFile : vcfFiles) {					
					try(BufferedReader reader = new BufferedReader(new FileReader(vcfFile))) {
						
						Map<String, Integer> infoHeaderMap = new HashMap<>();
						Map<String, Integer> formatHeaderMap = new HashMap<>();
						String[] normalHeaders;
						int colIndex = 0;
						int rowIndex = 0;
						// INFO 컬럼 이전 컬럼의 개수
						final int prefixColCount = 7;
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
							if (line.startsWith("##INFO")){
								String id = line.split("=<")[1].split(",")[0].split("=")[1];
								infoHeaderMap.put(id, 0);
							}
							if (line.startsWith("##FORMAT")){
								String id = line.split("=<")[1].split(",")[0].split("=")[1];
								formatHeaderMap.put(id, 0);
							}
							if (line.startsWith("#CHROM")){
								int tempColNum = 0;
								//INFO 해더 컬럼 인덱스 저장
								for(String k : infoHeaderMap.keySet()) {
									//System.out.println(prefixColCount + tempColNum);
									infoHeaderMap.put(k, prefixColCount + tempColNum);
									tempColNum += 1;
								}
								tempColNum = 0;
								//FORMAT 헤더 컬럼 인덱스 저장
								for(String k : formatHeaderMap.keySet()) {
									//System.out.println(prefixColCount + infoHeaderMap.size() + tempColNum);
									formatHeaderMap.put(k, prefixColCount + infoHeaderMap.size() + tempColNum);
									tempColNum += 1;
								}
								colIndex = 0;
								normalHeaders = line.split("\t");
								row = sheet.createRow(rowIndex);								
								for(int index = 0; index < prefixColCount; index++) {
									cell = row.createCell(colIndex);
									cell.setCellValue(normalHeaders[index]);
									colIndex += 1;
								}
								for(String k : infoHeaderMap.keySet()) {
									cell = row.createCell(colIndex);
									cell.setCellValue("INFO-" + k);
									colIndex += 1;
								}
								for(String k : formatHeaderMap.keySet()) {
									cell = row.createCell(colIndex);
									cell.setCellValue(k);
									colIndex += 1;
								}								
							}
							if (line.startsWith("chr")) {
								rowIndex += 1;
								row = sheet.createRow(rowIndex);
								colIndex = 0;
								String[] items = line.split("\t");
								String[] infoData = null;
								String[] formatKeys = null;
								String[] formatValues = null;
								for(String i : items) {
									if (colIndex == prefixColCount) {
										//INFO Col
										infoData = i.split(";");																				
									} else if (colIndex == prefixColCount + 1) {
										//FORMAT Col
										formatKeys = i.split(":");										
									} else if (colIndex == prefixColCount + 2) {
										//FORMAT Value Col
										formatValues = i.split(":"); 
									}else {									
										cell = row.createCell(colIndex);
										cell.setCellValue(i);										
									}
									colIndex += 1;
								}
								for(String info : infoData) {
									//System.out.println(info);
									String[] infoValues = info.split("=");
									if (infoValues.length > 1) {
										if (infoValues[1].length() < 32767) {
											cell = row.createCell(infoHeaderMap.get(infoValues[0]));
											cell.setCellValue(infoValues[1]);
										} else {
											cell = row.createCell(infoHeaderMap.get(infoValues[0]));
											cell.setCellValue(infoValues[1].substring(0, 32766));
										}
									} else if (infoValues.length == 1) {
										cell = row.createCell(infoHeaderMap.get(infoValues[0]));
										cell.setCellValue(".");
									}
								}
								
								for(int index = 0; index < formatKeys.length; index++) {
									//System.out.println(formatKeys[index] + ", " + formatValues[index] + ", " + formatHeaderMap.get(formatKeys[index]) );
									cell = row.createCell(formatHeaderMap.get(formatKeys[index]));
									cell.setCellValue(formatValues[index]);
								}
								rowIndex += 1;
							}							
						}
						File outFile = new File(vcfFile.getAbsolutePath().replace(".vcf", ".xlsx"));
						completeMessage += outFile.getAbsolutePath() + "\n";
						// Write the output to a file
					    FileOutputStream fileOut = new FileOutputStream(outFile);
					    wb.write(fileOut);
					    wb.close();
					    fileOut.close();
					    outputFiles.add(outFile);
					    completeCount += 1;
					    this.updateProgress(completeCount, outputFiles.size());
					} catch (Exception e) {
						e.printStackTrace();
						this.updateMessage("ERROR : " + e.getMessage());
					}
				}
				this.updateMessage(completeMessage);
				return outputFiles;
			}
			
		};
		convertProgressBar.progressProperty().bind(convertTask.progressProperty());
		convertStatusLabel.textProperty().bind(convertTask.messageProperty());
		Thread t = new Thread(convertTask);
		t.start();
	}
}
