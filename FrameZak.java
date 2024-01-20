
import java.awt.Cursor;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

// @author Timur
 
public class FrameZak extends javax.swing.JFrame {
    
    private Connection conn;
    private Statement stmt;
    private ResultSet rs;
    
    private String textJSON, sessionKey, sSession;    
    
    public FrameZak() {
        initComponents();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);   
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panTop = new javax.swing.JPanel();
        tfFIO = new javax.swing.JTextField();
        tfIDzak = new javax.swing.JTextField();
        tfLCod = new javax.swing.JTextField();
        tfDate = new javax.swing.JTextField();
        tfSkidka = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        panTop2 = new javax.swing.JPanel();
        bbZakChek = new javax.swing.JButton();
        chZakNal = new javax.swing.JCheckBox();
        chZakQR = new javax.swing.JCheckBox();
        chWozvrat = new javax.swing.JCheckBox();
        panBottom = new javax.swing.JPanel();
        bbSaveCsv = new javax.swing.JButton();
        tfSumma = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        tfTipNal = new javax.swing.JTextField();
        tfTipChek = new javax.swing.JTextField();
        lbIsp = new javax.swing.JLabel();
        bbSaveExcel = new javax.swing.JButton();
        panZakCentr = new javax.swing.JPanel();
        ScrollPanZak = new javax.swing.JScrollPane();
        tabZakazItem = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                ZakazAktiv(evt);
            }
        });

        panTop.setPreferredSize(new java.awt.Dimension(950, 41));
        panTop.addContainerListener(new java.awt.event.ContainerAdapter() {
            public void componentAdded(java.awt.event.ContainerEvent evt) {
                panTopComponentAdded(evt);
            }
        });
        panTop.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                panTopComponentShown(evt);
            }
        });

        tfFIO.setEditable(false);
        tfFIO.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tfFIO.setForeground(new java.awt.Color(0, 0, 153));

        tfIDzak.setEditable(false);
        tfIDzak.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tfIDzak.setForeground(new java.awt.Color(0, 0, 153));
        tfIDzak.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfIDzak.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfIDzakActionPerformed(evt);
            }
        });

        tfLCod.setEditable(false);
        tfLCod.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tfLCod.setForeground(new java.awt.Color(0, 0, 153));
        tfLCod.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        tfDate.setEditable(false);
        tfDate.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tfDate.setForeground(new java.awt.Color(0, 0, 153));
        tfDate.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        tfSkidka.setEditable(false);
        tfSkidka.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tfSkidka.setForeground(new java.awt.Color(0, 0, 153));
        tfSkidka.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfSkidka.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfSkidkaActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(153, 0, 0));
        jLabel2.setText("Скидка:");

        javax.swing.GroupLayout panTopLayout = new javax.swing.GroupLayout(panTop);
        panTop.setLayout(panTopLayout);
        panTopLayout.setHorizontalGroup(
            panTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panTopLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tfIDzak, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfDate, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfLCod, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfFIO, javax.swing.GroupLayout.PREFERRED_SIZE, 360, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfSkidka, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panTopLayout.setVerticalGroup(
            panTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panTopLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(panTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfIDzak, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfFIO, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfLCod, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfSkidka, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGap(5, 5, 5))
        );

        bbZakChek.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        bbZakChek.setForeground(new java.awt.Color(0, 0, 153));
        bbZakChek.setText("Отправить чек в ОФД");
        bbZakChek.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        bbZakChek.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbZakChekActionPerformed(evt);
            }
        });

        chZakNal.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        chZakNal.setForeground(new java.awt.Color(0, 0, 153));
        chZakNal.setText("Наличными");
        chZakNal.setBorderPaintedFlat(true);
        chZakNal.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        chZakNal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chZakNalActionPerformed(evt);
            }
        });

        chZakQR.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        chZakQR.setForeground(new java.awt.Color(0, 0, 153));
        chZakQR.setText("QR-код");
        chZakQR.setBorderPaintedFlat(true);
        chZakQR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chZakQRActionPerformed(evt);
            }
        });

        chWozvrat.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        chWozvrat.setForeground(new java.awt.Color(0, 0, 153));
        chWozvrat.setText("Чек возврата");
        chWozvrat.setBorderPaintedFlat(true);
        chWozvrat.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        chWozvrat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chWozvratActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panTop2Layout = new javax.swing.GroupLayout(panTop2);
        panTop2.setLayout(panTop2Layout);
        panTop2Layout.setHorizontalGroup(
            panTop2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panTop2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(chWozvrat, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(chZakNal, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(chZakQR, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(bbZakChek)
                .addGap(218, 218, 218))
        );
        panTop2Layout.setVerticalGroup(
            panTop2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panTop2Layout.createSequentialGroup()
                .addGroup(panTop2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bbZakChek, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panTop2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(chZakNal, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(chZakQR)
                        .addComponent(chWozvrat, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        bbSaveCsv.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbSaveCsv.setForeground(new java.awt.Color(0, 0, 153));
        bbSaveCsv.setText("Cохранить в csv");
        bbSaveCsv.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        bbSaveCsv.setEnabled(false);
        bbSaveCsv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbSaveCsvActionPerformed(evt);
            }
        });

        tfSumma.setEditable(false);
        tfSumma.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        tfSumma.setForeground(new java.awt.Color(0, 0, 153));
        tfSumma.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfSumma.setCaretColor(new java.awt.Color(0, 0, 153));
        tfSumma.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfSummaActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 0, 153));
        jLabel1.setText("Сумма");

        tfTipNal.setEditable(false);
        tfTipNal.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tfTipNal.setForeground(new java.awt.Color(0, 0, 153));
        tfTipNal.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfTipNal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfTipNalActionPerformed(evt);
            }
        });

        tfTipChek.setEditable(false);
        tfTipChek.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tfTipChek.setForeground(new java.awt.Color(0, 0, 153));
        tfTipChek.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfTipChek.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfTipChekActionPerformed(evt);
            }
        });

        lbIsp.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lbIsp.setForeground(new java.awt.Color(0, 0, 153));
        lbIsp.setText("Исп");

        bbSaveExcel.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        bbSaveExcel.setForeground(new java.awt.Color(0, 0, 153));
        bbSaveExcel.setText("Сохранить в xls");
        bbSaveExcel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        bbSaveExcel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbSaveExcelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panBottomLayout = new javax.swing.GroupLayout(panBottom);
        panBottom.setLayout(panBottomLayout);
        panBottomLayout.setHorizontalGroup(
            panBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panBottomLayout.createSequentialGroup()
                .addGap(100, 100, 100)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tfSumma, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(tfTipNal, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tfTipChek, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lbIsp, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bbSaveExcel)
                .addGap(12, 12, 12)
                .addComponent(bbSaveCsv)
                .addContainerGap())
        );
        panBottomLayout.setVerticalGroup(
            panBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panBottomLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bbSaveCsv)
                    .addComponent(tfSumma, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(tfTipNal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfTipChek, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbIsp)
                    .addComponent(bbSaveExcel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panZakCentr.setPreferredSize(new java.awt.Dimension(900, 500));

        tabZakazItem.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        tabZakazItem.setForeground(new java.awt.Color(0, 0, 153));
        tabZakazItem.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "№ пп", "Артикул", "Наименование", "Цена", "Кол-во", "Сумма", "Кол. Услуг"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Byte.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tabZakazItem.setRowHeight(34);
        tabZakazItem.setRowMargin(3);
        tabZakazItem.setShowGrid(true);
        tabZakazItem.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                tabZakazItemComponentShown(evt);
            }
        });
        ScrollPanZak.setViewportView(tabZakazItem);
        if (tabZakazItem.getColumnModel().getColumnCount() > 0) {
            tabZakazItem.getColumnModel().getColumn(2).setPreferredWidth(400);
        }

        javax.swing.GroupLayout panZakCentrLayout = new javax.swing.GroupLayout(panZakCentr);
        panZakCentr.setLayout(panZakCentrLayout);
        panZakCentrLayout.setHorizontalGroup(
            panZakCentrLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panZakCentrLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ScrollPanZak, javax.swing.GroupLayout.DEFAULT_SIZE, 982, Short.MAX_VALUE)
                .addContainerGap())
        );
        panZakCentrLayout.setVerticalGroup(
            panZakCentrLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panZakCentrLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ScrollPanZak, javax.swing.GroupLayout.DEFAULT_SIZE, 444, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panZakCentr, javax.swing.GroupLayout.DEFAULT_SIZE, 994, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panTop2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(panTop, javax.swing.GroupLayout.DEFAULT_SIZE, 988, Short.MAX_VALUE)
                    .addContainerGap()))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(panBottom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(49, 49, 49)
                .addComponent(panTop2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panZakCentr, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE)
                .addGap(85, 85, 85))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(panTop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 591, Short.MAX_VALUE)))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap(581, Short.MAX_VALUE)
                    .addComponent(panBottom, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap()))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tfIDzakActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfIDzakActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfIDzakActionPerformed

    private void tfSkidkaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfSkidkaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfSkidkaActionPerformed

    private void panTopComponentAdded(java.awt.event.ContainerEvent evt) {//GEN-FIRST:event_panTopComponentAdded
        // TODO add your handling code here:
    }//GEN-LAST:event_panTopComponentAdded

    private void panTopComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_panTopComponentShown
        // TODO add your handling code here:
    }//GEN-LAST:event_panTopComponentShown

    private void bbSaveCsvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbSaveCsvActionPerformed
        // TODO add your handling code here:
        String nameFiles = "temp\\zakaz_"+tfIDzak.getText()+".csv";
        String fText = "№ зак.;"+tfIDzak.getText()+";Дата: "+tfDate.getText()+";Скидка;"+tfSkidka.getText()+";;;\n";
        fText = fText + "Личный №;"+tfLCod.getText()+";"+tfFIO.getText()+";"+tfTipNal.getText()+";"+tfTipChek.getText()+";;;\n";
        fText = fText +"\n";
        fText = fText + "№ пп;Артикул;Наименование;Цена;Кол-во;Сумма;Услуг;\n";

        for (int i = 0; i < tabZakazItem.getRowCount(); i++) {
            for (int k = 0; k < 7; k++) {
                fText = fText + tabZakazItem.getValueAt(i, k).toString() + ";";
            }
            fText = fText + "\n";
        }
        fText = fText + ";;;;;" + tfSumma.getText() + ";;\n";

        try {
            File file = new File(nameFiles);
            file.delete();
            file.createNewFile();

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(fText);
                writer.close();

            } catch (IOException e) {
                System.out.println("Ошибка-1 при записи в файл");
                //e.printStackTrace();
            }

            System.out.println("Текст успешно записан в файл.");
        } catch (IOException e) {
            System.out.println("Ошибка-2 при записи в файл");
            //e.printStackTrace();
        }

        JOptionPane.showMessageDialog(new JFrame(), "Файл:\n\n" + nameFiles + "\n\ncоздан успешно!");        
    }//GEN-LAST:event_bbSaveCsvActionPerformed

    private void tfSummaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfSummaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfSummaActionPerformed

    private void tfTipNalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfTipNalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfTipNalActionPerformed

    private void tfTipChekActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfTipChekActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfTipChekActionPerformed

    private void ZakazAktiv(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_ZakazAktiv
        panZakCentr.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));                
        tfIDzak.setText(Integer.toString(NewJFrameMainMyservisProg.IdZak));
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

            //conn = DriverManager.getConnection(url, userName, password);
            conn = DriverManager.getConnection(NewJFrameMainMyservisProg.urlHostBD, NewJFrameMainMyservisProg.userNameBD, NewJFrameMainMyservisProg.passwordBD);
            stmt = conn.createStatement();
            //String query = "select date_zak,lcod,fio,sum_zak,proc_skid,tip_nal,tip_chek,isp from myservis.zakaz where id_zak="+tfIDzak.getText();
            String query = "select date_zak,lcod,fio,sum_zak,proc_skid,tip_nal,tip_chek,isp from myservis.zakaz where id_zak="+NewJFrameMainMyservisProg.IdZak;
            rs = stmt.executeQuery(query);

            while (rs.next()) {
                String sLCod = rs.getString("lcod");
                String sDate = rs.getString("date_zak");
                String fio = rs.getString("fio");
                //String sum_zak = rs.getString("sum_zak");
                String proc_skid = rs.getString("proc_skid");
                int tip_nal = rs.getInt("tip_nal");
                int tip_chek = rs.getInt("tip_chek");
                String isp = rs.getString("isp");
                
                tfLCod.setText(sLCod);
                tfFIO.setText(fio);
                tfDate.setText(sDate);
                tfSkidka.setText(proc_skid);
                lbIsp.setText(isp);
                
                if (tip_nal==1) {
                    tfTipNal.setText("Наличные");
                    chZakNal.setSelected(true);
                }
                if (tip_nal==2) {
                    tfTipNal.setText("QR-код");
                    chZakQR.setSelected(true);
                }
                if (tip_chek==1) tfTipChek.setText("Чек");
                if (tip_chek==2) tfTipChek.setText("БЧек");                                                        
            }
            rs.close();
            
            int SumZak = 0;
            DefaultTableModel dtm = (DefaultTableModel) tabZakazItem.getModel();            
            dtm.getDataVector().removeAllElements(); // удаляем все строки

            query = "select artikul,towar,cen,kol,kol_uslug from myservis.zakaz_item where id_zak="+NewJFrameMainMyservisProg.IdZak;
            rs = stmt.executeQuery(query);
            int i = 0;
            while (rs.next()) {
                i = i + 1;
                int artikul = rs.getInt("artikul");
                String towar = rs.getString("towar");
                int cen = rs.getInt("cen");
                int kol = rs.getInt("kol");
                int kolUslug = rs.getInt("kol_uslug");
                tabZakazItem.setRowHeight(i, 50);
                dtm.addRow(new Object[]{i, artikul,towar,cen,kol,cen*kol,kolUslug});
                SumZak = SumZak + cen*kol;
            }

            rs.close();
            stmt.close();

            String sZak = Integer.toString(SumZak);
            tfSumma.setText(sZak);

            if (i == 0) {
                dtm.getDataVector().removeAllElements(); // удаляем все строки
                dtm.addRow(new Object[]{"", "", "", "", "", "", "", "", "", "", ""});
                JFrame jfInfo = new JFrame();
                JOptionPane.showMessageDialog(jfInfo, "Заказов не обнаружено!");
            } else {
            }

        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SQLException ex) {
            System.err.println("Cannot connect to database server");
        } finally {
            if (conn != null) {
                try {
                    System.out.println("n***** Let terminate the Connection *****");
                    conn.close();
                    System.out.println("Database connection terminated... ");
                } catch (SQLException ex) {
                    System.out.println("Error in connection termination!");
                }
            }
        }
        bbSaveCsv.setEnabled(true);
        panZakCentr.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_ZakazAktiv

    private void tabZakazItemComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_tabZakazItemComponentShown
        // TODO add your handling code here:
    }//GEN-LAST:event_tabZakazItemComponentShown

    private void chZakNalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chZakNalActionPerformed
        // TODO add your handling code here:
        if (chZakNal.isSelected()) {
            chZakQR.setSelected(false);
        } else {
            chZakQR.setSelected(true);
        }
    }//GEN-LAST:event_chZakNalActionPerformed

    private void chZakQRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chZakQRActionPerformed
        // TODO add your handling code here:
        if (chZakQR.isSelected()) {
            chZakNal.setSelected(false);
        } else {
            chZakNal.setSelected(true);
        }
    }//GEN-LAST:event_chZakQRActionPerformed

    private void chWozvratActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chWozvratActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_chWozvratActionPerformed

    private void bbZakChekActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbZakChekActionPerformed
        String ObjButtons[] = {"Yes", "No"};
        int PromptResult = JOptionPane.showOptionDialog(null,
                    "Действительно отправить Чек в ОФД?", "Подтверждение отправки Чека в ОФД",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null,
                    ObjButtons, ObjButtons[1]);
        if (PromptResult == 0) {

        bbZakChek.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));        
        //httpKKM = "http://localhost:50010/api.json";        
        //sKKMkassirINN = "00000000000000000000";
        //sKKMmercComPort = "COM4";
        //sKKMmercModel = "185F";
        
        sessionKey = "";
                
        System.out.println("******************************");
        System.out.println("Пролучение ключа сесиии:");
        System.out.println("******************************");
        
        try {
            URL urlKKT = new URL(NewJFrameMainMyservisProg.httpKKM);
            textJSON = "{\"sessionKey\": null, \"command\": \"OpenSession\", \"portName\": \"" + NewJFrameMainMyservisProg.sKKMmercComPort + "\", \"model\": \"" + NewJFrameMainMyservisProg.sKKMmercModel + "\"}";
            System.out.println(textJSON);
            System.out.println("------------------------------");
            
            HttpURLConnection connKKT = (HttpURLConnection) urlKKT.openConnection();
            connKKT.setRequestMethod("POST");
            connKKT.setConnectTimeout(5000);
            connKKT.setDoOutput(true);
            connKKT.setRequestProperty("Content-Type", "'application/json; charset=utf-8");
            connKKT.setUseCaches(false);

            try (DataOutputStream dos = new DataOutputStream(connKKT.getOutputStream())) {
                dos.writeBytes(textJSON);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(connKKT.getInputStream(), "UTF-8"))) {

                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                    System.out.println("------------------------------");
                    JSONParser jsonParser = new JSONParser();
                    JSONObject jsonObject = (JSONObject) jsonParser.parse(line);
                    // получение строки из объекта
                    String sSessionKey = (String) jsonObject.get("sessionKey");
                    long sLong = (long) jsonObject.get("result");
                    String sResult = Long.toString(sLong);

                    sessionKey = sSessionKey;

                    if (sLong == 0) {
                        sSession = "open";
                    } else {
                        sSession = "close";
                    }
                    String sDescription = (String) jsonObject.get("description");
                    
                    System.out.println("     key     /rezult/Description");
                    System.out.println(sSessionKey + "/" + sResult + "/" + sDescription);
                    System.out.println("sSession: "+sSession);
                    System.out.println("------------------------------");
                }
            } catch (ParseException ex) {
                Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException e) {}   
        
        //Открытие Чека  
        System.out.println("Открытие Чека:");
        System.out.println("------------------------------");
        try {
            // checkType: 0-Приход, 1-Возврат прихода
            // taxSystem: 0-Общая, 1- Упрощенная доходы
            
            if (chWozvrat.isSelected()) {
            textJSON = "{\"sessionKey\": \"" + sessionKey + "\", \"command\": \"OpenCheck\", \"checkType\": 1, \"printDoc\": true, \"taxSystem\": 1 ,\"cashierInfo\": {\"+cashierName+\": \"" + strPreobraz(NewJFrameMainMyservisProg.aktPolKKT) + "\", \"cashierINN\": \"" + NewJFrameMainMyservisProg.sKKMkassirINN + "\"} }";
            }else{
            textJSON = "{\"sessionKey\": \"" + sessionKey + "\", \"command\": \"OpenCheck\", \"checkType\": 0, \"printDoc\": true, \"taxSystem\": 1 ,\"cashierInfo\": {\"+cashierName+\": \"" + strPreobraz(NewJFrameMainMyservisProg.aktPolKKT) + "\", \"cashierINN\": \"" + NewJFrameMainMyservisProg.sKKMkassirINN + "\"} }";    
            }
            System.out.println(textJSON);
            System.out.println("------------------------------");
            
            URL urlKKT = new URL(NewJFrameMainMyservisProg.httpKKM);
            HttpURLConnection connKKT = (HttpURLConnection) urlKKT.openConnection();
            connKKT.setRequestMethod("POST");
            connKKT.setConnectTimeout(5000);
            connKKT.setDoOutput(true);
            connKKT.setRequestProperty("Content-Type", "'application/json; charset=utf-8");
            connKKT.setUseCaches(false);
            
            try (DataOutputStream dos = new DataOutputStream(connKKT.getOutputStream())) {
                dos.writeBytes(textJSON);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(connKKT.getInputStream(), "UTF-8"))) {

                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                    System.out.println("------------------------------");
                    JSONParser jsonParser = new JSONParser();
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = (JSONObject) jsonParser.parse(line);
                    } catch (ParseException ex) {
                        Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    long lRezult = (long) jsonObject.get("result");
                    if (lRezult == 0) {
                        System.out.println("Открытие Чека произведено успешно!");
                    } else {
                        System.out.println("Открытие Чека не произведено.");
                    }

                    String sDescription = (String) jsonObject.get("description");
                    System.out.println("Description:" + sDescription);
                    System.out.println("------------------------------");
                }
            }
        } catch (IOException e) {}
        
        //Добавление товара
        
        System.out.println("Добавление товаров:");
        System.out.println("------------------------------");
        
        try {
            URL urlKKT = new URL(NewJFrameMainMyservisProg.httpKKM);
            
            HttpURLConnection connKKT = (HttpURLConnection) urlKKT.openConnection();
            connKKT.setRequestMethod("POST");
            connKKT.setDoOutput(true);
            connKKT.setConnectTimeout(10000);
            connKKT.setRequestProperty("Content-Type", "'application/json; charset=utf-8");
            connKKT.setUseCaches(false);
            
            int summaZak=0;
            for (int i = 0; i < tabZakazItem.getRowCount(); i++) {
                String sKol = tabZakazItem.getValueAt(i, 4).toString();
                String sCena = tabZakazItem.getValueAt(i, 3).toString();
                String sArtikul = tabZakazItem.getValueAt(i, 1).toString();
                int kol = Integer.parseInt(sKol);
                int cena = Integer.parseInt(sCena);
                int artikul = Integer.parseInt(sArtikul);
                int ssSekciya;
                int ssPredmet;
                if (((artikul > 5000) & (artikul < 6000)) | ((artikul > 9000) & (artikul < 10000))) {
                    // Это товары
                    ssSekciya = 2;
                    ssPredmet = 1;
                } else {
                    // Это услуги
                    ssSekciya = 1;
                    ssPredmet = 4;
                }                
                int summa = kol*cena;
                summaZak=summaZak+summa;
                
                String ssTowar = "\"productName\": \"" + strPreobraz(tabZakazItem.getValueAt(i, 2).toString()) + "\", \"qty\": " + kol * 10000 + " , \"taxCode\": 6, \"price\":" + cena * 100 + ", \"section\":" + ssSekciya + ", \"productTypeCode\":" + ssPredmet;
                
                textJSON = "{\"sessionKey\": \"" + sessionKey + "\", \"command\": \"AddGoods\"," + ssTowar + "}";
                System.out.println(textJSON);
                System.out.println("------------------------------");
                        
                try (DataOutputStream dos = new DataOutputStream(connKKT.getOutputStream())) {
                    dos.writeBytes(textJSON);
                }

                try (BufferedReader br = new BufferedReader(new InputStreamReader(connKKT.getInputStream(), "UTF-8"))) {

                    String line;
                    while ((line = br.readLine()) != null) {
                        System.out.println(line);
                        System.out.println("------------------------------");
                        JSONParser jsonParser = new JSONParser();
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = (JSONObject) jsonParser.parse(line);
                        } catch (ParseException ex) {
                            Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        long lRezult = (long) jsonObject.get("result");
                        if (lRezult == 0) {
                            System.out.println("Добавления товара произведено успешно.");
                            System.out.println("------------------------------");
                        } else {
                            System.out.println("Добавления товара не произведено!!!");
                            System.out.println("------------------------------");
                        }

                        String sDescription = (String) jsonObject.get("description");
                        System.out.println("Description:" + sDescription);
                    }
                }
            }
        } catch (IOException e) {}
        
        //Закрытие Чека
        System.out.println("Закрытие чека");
        System.out.println("------------------------------");
        
        try {
            URL urlKKT = new URL(NewJFrameMainMyservisProg.httpKKM);
            int summaKKM = Integer.parseInt(tfSumma.getText());
            summaKKM = summaKKM * 100;
            
            if (chZakNal.isSelected()) {
                textJSON = "{\"sessionKey\": \"" + sessionKey + "\", \"command\": \"CloseCheck\", \"payment\": { \"cash\": " + summaKKM + " } }";
            } else {
                textJSON = "{\"sessionKey\": \"" + sessionKey + "\", \"command\": \"CloseCheck\", \"payment\": { \"ecash\": " + summaKKM + " } }";
            }        
            System.out.println(textJSON);
            System.out.println("------------------------------");
            
            HttpURLConnection connKKT = (HttpURLConnection) urlKKT.openConnection();
            connKKT.setRequestMethod("POST");
            connKKT.setConnectTimeout(5000);
            connKKT.setDoOutput(true);
            connKKT.setRequestProperty("Content-Type", "'application/json; charset=utf-8");
            connKKT.setUseCaches(false);

            try (DataOutputStream dos = new DataOutputStream(connKKT.getOutputStream())) {
                dos.writeBytes(textJSON);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(connKKT.getInputStream(), "UTF-8"))) {

                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                    System.out.println("------------------------------");    
                    JSONParser jsonParser = new JSONParser();
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = (JSONObject) jsonParser.parse(line);
                    } catch (ParseException ex) {
                        Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    long lRezult = (long) jsonObject.get("result");
                    if (lRezult == 0) {
                        System.out.println("Закрытие Чека произведено успешно!");
                    } else {
                        System.out.println("Закрытие Чека не произведено.");
                    }
                    System.out.println("------------------------------");

                    String sDescription = (String) jsonObject.get("description");
                    System.out.println("Description:" + sDescription);
                }
            }
        } catch (IOException e) {}
       
        // Закрытие сессии
        System.out.println("Закрытие сессии:");
        System.out.println("------------------------------");
        
        try {
            URL urlKKT = new URL(NewJFrameMainMyservisProg.httpKKM);
            textJSON = "{\"sessionKey\": \"" + sessionKey + "\",\"command\": \"CloseSession\"}";
            System.out.println(textJSON);
            System.out.println("------------------------------");

            HttpURLConnection connKKT = (HttpURLConnection) urlKKT.openConnection();
            connKKT.setRequestMethod("POST");
            connKKT.setConnectTimeout(5000);
            connKKT.setDoOutput(true);
            connKKT.setRequestProperty("Content-Type", "'application/json; charset=utf-8");
            connKKT.setUseCaches(false);

            try (DataOutputStream dos = new DataOutputStream(connKKT.getOutputStream())) {
                dos.writeBytes(textJSON);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(connKKT.getInputStream(), "UTF-8"))) {

                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                    System.out.println("----------------------------------------------");

                    JSONParser jsonParser = new JSONParser();
                    JSONObject jsonObject = (JSONObject) jsonParser.parse(line);
                    // получение строки из объекта
                    long sLong = (long) jsonObject.get("result");
                    String sResult = Long.toString(sLong);
                    String sDescription = (String) jsonObject.get("description");

                    System.out.println("rezult/Description");
                    System.out.println(sResult + "/" + sDescription);

                    if (sLong == 0) {
                        sSession = "close";
                    } else {
                        sSession = "none";
                    }

                }
            } catch (ParseException ex) {
                Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException e) {
        }
        System.out.println("******************************");
        System.out.println("Закрытие сессии выполнено успешно!");
        System.out.println("******************************");  
        bbZakChek.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }else{
            System.out.println("******************************");
            System.out.println("Отказ от отправки чека в ОФД.");
            System.out.println("******************************");
        }
    }//GEN-LAST:event_bbZakChekActionPerformed

    private void bbSaveExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbSaveExcelActionPerformed
        String nameFiles = "temp\\Заказ-"+tfFIO.getText()+".xls";

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Заказ");

        // создаем шрифт
        HSSFFont font1 = workbook.createFont();
        HSSFFont font2 = workbook.createFont();

        font1.setFontHeightInPoints((short) 12);
        //font.setFontName("Courier New");
        font1.setFontName("Arial");
        //font.setBold(true);

        font2.setFontHeightInPoints((short) 11);
        font2.setFontName("Arial");
        font2.setBold(true);

        // создаем стиль для ячеек основной таблицы и для ячеек в заголовке таблицы
        HSSFCellStyle style = workbook.createCellStyle();
        HSSFCellStyle style2 = workbook.createCellStyle();
        HSSFCellStyle style3 = workbook.createCellStyle();
        // и применяем к этому стилю жирный шрифт
        style3.setFont(font1);  // Для Шапки
        style2.setFont(font2);  // Для заголовка таблицы

        // Настройка выравнивания стиля
        // Для основной таблицы
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // Для заголовка таблицы
        style2.setAlignment(HorizontalAlignment.CENTER);
        style2.setVerticalAlignment(VerticalAlignment.CENTER);

        // Для шапки
        style3.setAlignment(HorizontalAlignment.LEFT);
        style3.setVerticalAlignment(VerticalAlignment.CENTER);

        HSSFRow row = sheet.createRow((short) 0);

        //прописываем шапку документа
        row.createCell(0).setCellValue("№ заказа");
        row.createCell(1).setCellValue(tfIDzak.getText());
        row.createCell(2).setCellValue(tfFIO.getText());
        row.createCell(3).setCellValue("Дата заказа");
        row.createCell(4).setCellValue(tfDate.getText());
        
        row.getCell(0).setCellStyle(style3);        
        row.getCell(1).setCellStyle(style3);
        row.getCell(2).setCellStyle(style3);
        row.getCell(3).setCellStyle(style3);
        row.getCell(4).setCellStyle(style3);
        row.setHeightInPoints(30);
        
        // стиль для выделения ячейки со всех сторон
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);

        // стиль-2 для выделения ячейки со всех сторон
        style2.setBorderTop(BorderStyle.THIN);
        style2.setBorderRight(BorderStyle.THIN);
        style2.setBorderBottom(BorderStyle.THIN);
        style2.setBorderLeft(BorderStyle.THIN);

        style2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style2.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());  // серый цвет фона для заголовка таблицы

        // создаем строку с заголовком
        row = sheet.createRow((short) 2);
        row.createCell(0).setCellValue("№ пп");
        row.createCell(1).setCellValue("Артикул");
        row.createCell(2).setCellValue("Наименование");
        row.createCell(3).setCellValue("Цена");
        row.createCell(4).setCellValue("Кол-во");
        row.createCell(5).setCellValue("Кол. Услуг");

        // Устанавливаем высоту и стили для заголовка таблицы
        
        for (int k = 0; k < tabZakazItem.getColumnCount()-1; k++) {
            row.getCell(k).setCellStyle(style2);
            row.setHeightInPoints(22);
        }

        // прописываем основную таблицу
        
        for (int i = 0; i < tabZakazItem.getRowCount(); i++) {
            row = sheet.createRow((short) i + 3);
            for (int k = 0; k < tabZakazItem.getColumnCount()-1; k++) {
                row.createCell(k).setCellValue(tabZakazItem.getValueAt(i, k).toString());
                row.getCell(k).setCellStyle(style);
                row.setHeightInPoints(18);
            }
        }
        
        row = sheet.createRow((short) tabZakazItem.getRowCount() + 4);
        row.createCell(4).setCellValue("Итого сумма:");
        row.createCell(5).setCellValue(tfSumma.getText());
        row.getCell(4).setCellStyle(style3);
        row.getCell(5).setCellStyle(style3);
        row.setHeightInPoints(24);

        row = sheet.createRow((short) tabZakazItem.getRowCount() + 5);
        row.createCell(4).setCellValue("Тип оплаты:");
        row.createCell(5).setCellValue(tfTipNal.getText());
        row.getCell(4).setCellStyle(style3);
        row.getCell(5).setCellStyle(style3);
        row.setHeightInPoints(24);

        row = sheet.createRow((short) tabZakazItem.getRowCount() + 6);
        row.createCell(4).setCellValue("Исполнитель:");
        row.createCell(5).setCellValue(lbIsp.getText());
        row.getCell(4).setCellStyle(style3);
        row.getCell(5).setCellStyle(style3);
        row.setHeightInPoints(24);

        // Устанавливаем автоширину для всех колонок

        for (int k = 0; k < tabZakazItem.getColumnCount()-1; k++) {
            sheet.autoSizeColumn(k);
        }

        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(nameFiles);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            workbook.write(fileOut);
        } catch (IOException ex) {
            Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            fileOut.close();
        } catch (IOException ex) {
            Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            workbook.close();
        } catch (IOException ex) {
            Logger.getLogger(NewJFrameMainMyservisProg.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Your excel file has been generated!");

        JOptionPane.showMessageDialog(new JFrame(), "Файл:\n\n" + nameFiles + "\n\nCоздан успешно!");
    }//GEN-LAST:event_bbSaveExcelActionPerformed

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FrameZak.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrameZak.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrameZak.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrameZak.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FrameZak().setVisible(true);
            }
        });
    }
    
    static public String strKiril(char ch) {
        if ((ch == 'А') || (ch == 'Б') || (ch == 'В') || (ch == 'Г') || (ch == 'Д') || (ch == 'Е') || (ch == 'Ё') || (ch == 'Ж') || (ch == 'З') || (ch == 'И') || (ch == 'К') || (ch == 'Л') || (ch == 'М') || (ch == 'Н') || (ch == 'О') || (ch == 'П') || (ch == 'Р') || (ch == 'С') || (ch == 'Т') || (ch == 'У') || (ch == 'Ф') || (ch == 'Х') || (ch == 'Ц') || (ch == 'Ч') || (ch == 'Ш') || (ch == 'Щ') || (ch == 'Ь') || (ch == 'Ы') || (ch == 'Ъ') || (ch == 'Э') || (ch == 'Ю') || (ch == 'Я') || (ch == 'Й')
                || (ch == 'а') || (ch == 'б') || (ch == 'в') || (ch == 'г') || (ch == 'д') || (ch == 'е') || (ch == 'ё') || (ch == 'ж') || (ch == 'з') || (ch == 'и') || (ch == 'к') || (ch == 'л') || (ch == 'м') || (ch == 'н') || (ch == 'о') || (ch == 'п') || (ch == 'р') || (ch == 'с') || (ch == 'т') || (ch == 'у') || (ch == 'ф') || (ch == 'х') || (ch == 'ц') || (ch == 'ч') || (ch == 'ш') || (ch == 'щ') || (ch == 'ь') || (ch == 'ы') || (ch == 'ъ') || (ch == 'э') || (ch == 'ю') || (ch == 'я') || (ch == 'й')) {
            return "1";
        } else {
            return "";
        }
    }

    static public String strPreobraz(String str) {
        String resKir;
        String resTek = "";
        for (int i = 0; i < str.length(); i++) {
            //char charCode = inString.charAt(i);
            char c = str.charAt(i);
            //System.out.println(char2);
            resKir = strKiril(c);
            if ("1".equals(resKir)) {
                resTek += String.format("\\u%04x", (int) str.charAt(i));
            } else {
                resTek += c;
            }
        }
        return resTek;
    }
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane ScrollPanZak;
    private javax.swing.JButton bbSaveCsv;
    private javax.swing.JButton bbSaveExcel;
    private javax.swing.JButton bbZakChek;
    private javax.swing.JCheckBox chWozvrat;
    private javax.swing.JCheckBox chZakNal;
    private javax.swing.JCheckBox chZakQR;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel lbIsp;
    private javax.swing.JPanel panBottom;
    private javax.swing.JPanel panTop;
    private javax.swing.JPanel panTop2;
    private javax.swing.JPanel panZakCentr;
    private javax.swing.JTable tabZakazItem;
    private javax.swing.JTextField tfDate;
    private javax.swing.JTextField tfFIO;
    private javax.swing.JTextField tfIDzak;
    private javax.swing.JTextField tfLCod;
    private javax.swing.JTextField tfSkidka;
    private javax.swing.JTextField tfSumma;
    private javax.swing.JTextField tfTipChek;
    private javax.swing.JTextField tfTipNal;
    // End of variables declaration//GEN-END:variables
}
