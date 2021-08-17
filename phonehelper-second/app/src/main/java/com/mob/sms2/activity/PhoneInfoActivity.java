package com.mob.sms2.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mob.sms2.R;
import com.mob.sms2.adapter.PhoneInfoAdapter;
import com.mob.sms2.base.BaseActivity;
import com.mob.sms2.bean.PhoneInfoBean;
import com.mob.sms2.db.CallContactTable;
import com.mob.sms2.db.DatabaseBusiness;
import com.mob.sms2.db.SmsContactTable;
import com.mob.sms2.utils.ToastUtil;
import com.youth.banner.util.LogUtils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

// 电话导入页面
public class PhoneInfoActivity extends BaseActivity {
    @BindView(R.id.title)
    TextView mTitleTv;
    @BindView(R.id.num)
    TextView mNumTv;
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private ArrayList<PhoneInfoBean> mDatas = new ArrayList<>();
    private PhoneInfoAdapter mPhoneInfoAdapter;
    private int mType;//0 word 1 excel 2 txt 3复制粘贴
    private String mType2;//sms  call
    private String mFilePath;
    private String mCopyContent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phono_info);
        ButterKnife.bind(this);
        setStatusBar(getResources().getColor(R.color.green));
        initView();
    }

    private void initView() {
        mTitleTv.setText(getIntent().getStringExtra("title"));
        mType = getIntent().getIntExtra("type", 0);
        mFilePath = getIntent().getStringExtra("filePath");
        mCopyContent = getIntent().getStringExtra("copyContent");
        mType2 = getIntent().getStringExtra("type2");

        if (mType == 0) {
            if (mFilePath.endsWith("docx")) {
                readDocx();
            }else {
                readWord(mFilePath);
            }
        } else if (mType == 1) {
            try {
                if (mFilePath.endsWith("xlsx")) {
                    readXlsx(new File(mFilePath));
                } else if (mFilePath.endsWith("xls")) {
                    readXls(mFilePath);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if (mType == 2) {
            readTxtFile(mFilePath);
        } else if (mType == 3) {
            if (mCopyContent.contains("\n")) {
                int length = mCopyContent.split("\n").length;
                for (int i = 0; i < length; i++) {
                    mDatas.add(new PhoneInfoBean(mCopyContent.split("\n")[i], "无姓名", false));
                }
            } else {
                mDatas.add(new PhoneInfoBean(mCopyContent, "无姓名", false));
            }
        }
        mNumTv.setText(mDatas.size() + "个联系人");
        mPhoneInfoAdapter = new PhoneInfoAdapter(this, mDatas);
        mRecyclerView.setAdapter(mPhoneInfoAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mPhoneInfoAdapter.setOnItemClickLsitener(new PhoneInfoAdapter.OnItemClickListener() {
            @Override
            public void onclick(int position) {
                mDatas.get(position).isSelected = !mDatas.get(position).isSelected;
                mPhoneInfoAdapter.notifyDataSetChanged();
            }
        });
    }

    private void readDocx() {
        FileInputStream in;
        String text = null;
        try {
            in = new FileInputStream(new File(mFilePath));
            XWPFDocument doc = new XWPFDocument(in);
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
            text = extractor.getText();
            if (text.contains("\n")) {
                String[] split = text.split("\n");
                for (String s : split) {
                    mDatas.add(new PhoneInfoBean(s, "无姓名", false));
                }
            } else {
                mDatas.add(new PhoneInfoBean(text, "无姓名", false));
            }
        } catch (Exception e) {
            System.out.println("ppppp报错ppppp");
            e.printStackTrace();
            System.out.println("------ppppp报错ppppp-----");
        }
    }

    public void readXlsx(File file) throws FileNotFoundException {
        if (file == null) {
            Log.e("NullFile", "读取Excel出错，文件为空文件");
            return;
        }
        InputStream stream = new BufferedInputStream(new FileInputStream(file));
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(stream);
            Sheet sheet = workbook.getSheetAt(0);
            int rowsCount = sheet.getPhysicalNumberOfRows();
            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
            for (int r = 0; r < rowsCount; r++) {
                Row row = sheet.getRow(r);
                int cellsCount = row.getPhysicalNumberOfCells();
                //每次读取一行的内容
                for (int c = 0; c < cellsCount; c++) {
                    //将每一格子的内容转换为字符串形式
                    String value = getCellAsString(row, c, formulaEvaluator);
                    mDatas.add(new PhoneInfoBean(value, "无姓名", false));
                }
            }
        } catch (Exception e) {
            /* proper exception handling to be here */
            System.out.println("xxxxx爆粗xxxx");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

    private static String getCellAsString(Row row, int c, FormulaEvaluator formulaEvaluator) {
        String value = "";
        try {
            Cell cell = row.getCell(c);
            CellValue cellValue = formulaEvaluator.evaluate(cell);
            switch (cellValue.getCellType()) {
                case Cell.CELL_TYPE_NUMERIC:
//                case NUMERIC:
                    double strCell = cell.getNumericCellValue();
                    DecimalFormat formatCell = (DecimalFormat) NumberFormat.getPercentInstance();
                    formatCell.applyPattern("0");
                    value = formatCell.format(strCell);
                    if (Double.parseDouble(value) != strCell) {
                        formatCell.applyPattern(Double.toString(strCell));
                        value = formatCell.format(strCell);
                    }
                    break;
                case Cell.CELL_TYPE_STRING:
//                case STRING:
                    value = "" + cellValue.getStringValue();
                    break;
                default:
                    break;
            }
        } catch (NullPointerException e) {
            /* proper error handling should be here */
            LogUtils.e(e.toString());
        }
        return value;
    }

    private void readXls(String strFilePath) {
        File file = new File(strFilePath);
        try {
            Workbook workbook = new HSSFWorkbook(new FileInputStream(file));
            Sheet sheet = workbook.getSheetAt(0);
            int sheetRows = sheet.getLastRowNum();
            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
            for (int i = 0; i < sheetRows; i++) {
                String cellAsString = getCellAsString(sheet.getRow(i), 0, formulaEvaluator);
                mDatas.add(new PhoneInfoBean(cellAsString, "无姓名", false));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readWord(String strFilePath) {
        FileInputStream in;
        String text = null;
        try {
            in = new FileInputStream(new File(strFilePath));
            WordExtractor extractor = new WordExtractor(in);
            text = extractor.getText();
            if (text.contains("\n")) {
                String[] split = text.split("\n");
                for (String s : split) {
                    mDatas.add(new PhoneInfoBean(s, "无姓名", false));
                }
            } else {
                mDatas.add(new PhoneInfoBean(text, "无姓名", false));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readTxtFile(String strFilePath) {
        String path = strFilePath;
        //打开文件
        File file = new File(path);
        //如果path是传递过来的参数，可以做一个非目录的判断
        try {
            InputStream instream = new FileInputStream(file);
            if (instream != null) {
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line;
                //分行读取
                while ((line = buffreader.readLine()) != null) {
                    mDatas.add(new PhoneInfoBean(line, "无姓名", false));
                }
                instream.close();
            }
        } catch (Exception e) {
        }
    }

    @OnClick({R.id.back, R.id.delete, R.id.confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.delete:
                delete();
                break;
            case R.id.confirm:
                confirm();
                break;
        }
    }

    private void delete() {
        ArrayList<PhoneInfoBean> datas = new ArrayList<>();
        for (int i = 0; i < mDatas.size(); i++) {
            if (mDatas.get(i).isSelected) {
                datas.add(mDatas.get(i));
            }
        }
        if (datas.size() > 0) {
            mDatas.removeAll(datas);
            mPhoneInfoAdapter.notifyDataSetChanged();
            mNumTv.setText(mDatas.size() + "个联系人");
        } else {
            ToastUtil.show("请选择要删除的联系人电话");
        }
    }

    private void confirm() {
        if (mDatas.size() > 0) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("请稍后...");
            progressDialog.show();
            new Thread() {
                @Override
                public void run() {
                    insertDb();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            finish();
                        }
                    });
                }
            }.start();
        } else {
            ToastUtil.show("请选择联系人电话");
        }
    }

    private void insertDb() {
        if ("call".equals(mType2)) {
            List<CallContactTable> callContactTables = DatabaseBusiness.getCallContacts();
            if (callContactTables.size() > 0) {
                for (CallContactTable callContactTable : callContactTables) {
                    DatabaseBusiness.delCallContact(callContactTable);
                }
            }
            for (int i = 0; i < mDatas.size(); i++) {
                DatabaseBusiness.createCallContact(new CallContactTable(mDatas.get(i).name, mDatas.get(i).phone));
            }
        } else if ("sms".equals(mType2)) {
            List<SmsContactTable> smsContactTables = DatabaseBusiness.getSmsContacts();
            if (smsContactTables.size() > 0) {
                for (SmsContactTable smsContactTable : smsContactTables) {
                    DatabaseBusiness.delSmsContact(smsContactTable);
                }
            }
            for (int i = 0; i < mDatas.size(); i++) {
                DatabaseBusiness.createSmsContact(new SmsContactTable(mDatas.get(i).name, mDatas.get(i).phone));
            }
        }
    }
}
