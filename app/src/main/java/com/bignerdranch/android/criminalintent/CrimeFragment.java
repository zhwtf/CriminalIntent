package com.bignerdranch.android.criminalintent;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.view.MenuInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.ShareCompat;
import android.widget.ImageButton;
import android.widget.ImageView;


import java.io.File;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.UUID;

/**
 * Created by zhenghao on 2017-06-28.
 */

public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_PHOTO = 2;

    public static final String EXTRA_DELETE_CRIME = "delete_crime";



    private Crime mCrime;
    private EditText mTitleField;

    /*
    获取图片文件位置
     */
    private File mPhotoFile;

    private Button mDateButton;
    private CheckBox mSolvedCheckBox;
    /*
    发送消息按钮
     */
    private Button mReportButton;
    /*
    选择联系人按钮
     */
    private Button mSuspectButton;

    /*
    拨打嫌疑人电话
     */
    private Button mCallSuspectButton;


    //添加image部分
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;




    //将数据存放在fragment的argument bundle中
    //便于复用
    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set the toolbar
        setHasOptionsMenu(true);
        //mCrime = new Crime();
        //UUID crimeId = (UUID) getActivity().getIntent().getSerializableExtra(CrimeActivity.EXTRA_CRIME_ID);
        //从argument中获取crime ID
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime); //获取图片文件位置
    }

    @Override
    public void onPause() {
        super.onPause();

        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {
        //super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = (EditText)v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // This space intentionally left blank
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());

            }

            @Override
            public void afterTextChanged(Editable s) {
                //This one too
            }
        });

        mDateButton = (Button) v.findViewById(R.id.crime_date);
        updateDate();
        //mDateButton.setEnabled(false);

        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                //DatePickerFragment dialog = new DatePickerFragment();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);

            }
        });

        mSolvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Set the crime's solved property
                mCrime.setSolved(isChecked);
            }
        });

        mReportButton = (Button) v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));

                使用隐式intent启动activity时，也可以创建每次都显示的activity选择器。和以前一样创建隐
式intent后，调用以下Intent方法并传入创建的隐式intent以及用作选择器标题的字符串

                i = Intent.createChooser(i, getString(R.string.send_report));
                */



                // 通过ShareCompat.IntentBuilder来创建Intent
                Intent i = ShareCompat.IntentBuilder.from(getActivity())
                        .setText(getCrimeReport())
                        .setSubject(getString(R.string.crime_report_subject))
                        .setType("text/plain")
                        .getIntent();
                startActivity(i);


            }
        });
        /*
        以上代码使用了一个接受字符串参数的Intent构造方法，我们传入的是一个定义操作的常
量。取决于要创建的隐式intent类别，也可以使用一些其他形式的构造方法。关于其他intent构造
方法及其使用说明，可以查阅Intent参考文档。因为没有接受数据类型的构造方法可用，所以
必须专门设置它。
消息内容和主题是作为extra附加到intent上的。注意，这些extra信息使用了Intent类中定
义的常量。因此，任何响应该intent的activity都知道这些常量，自然也知道该如何使用它们的
关联值。
         */

        /*
        Challenge: call a suspect
        https://forums.bignerdranch.com/t/challenge-call-a-suspect/7828
         */
        mCallSuspectButton = (Button) v.findViewById(R.id.call_suspect);
        mCallSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri contentUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                String selectClause = ContactsContract.CommonDataKinds.Phone.CONTACT_ID +
                        " = ?";
                String[] fields = new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER};
                String[] selectParams = new String[] {Long.toString(mCrime.getContactId())};

                Cursor cursor = getActivity().getContentResolver().query(contentUri, fields, selectClause, selectParams, null);

                if (cursor != null) {
                    try {
                        if (cursor.getCount() == 0) {
                            return;
                        }
                        cursor.moveToFirst();
                        String number = cursor.getString(0);
                        Uri phoneNumber = Uri.parse("tel:" + number);
                        Intent i = new Intent(Intent.ACTION_DIAL, phoneNumber);
                        startActivity(i);
                        /*
                        PackageManager packageManager = getActivity().getPackageManager();
                        if (packageManager.resolveActivity(i, PackageManager.MATCH_DEFAULT_ONLY)
                                == null) {
                            mCallSuspectButton.setEnabled(false);
                        }
                        */

                    } finally {
                        cursor.close();
                    }
                }
            }
        });


        /*
        现在，创建另一个隐式intent，实现让用户从联系人应用里选择嫌疑人。新建的隐式intent将
由操作以及数据获取位置组成。操作为Intent.ACTION_PICK。联系人数据获取位置为
ContactsContract.Contacts.CONTENT_URI。简而言之，就是请Android帮忙从联系人数据库里
获取某个具体联系人。
         */
        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        //不允许匹配intent－－－》加入code
        //pickContact.addCategory(Intent.CATEGORY_HOME);
        mSuspectButton = (Button) v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });

        if (mCrime.getSuspect() != null) {
            mSuspectButton.setText(mCrime.getSuspect());

        }

        //检查是否存在联系人应用
        /*
        Android设备上安装了哪些组件以及包括哪些activity，PackageManager类全都知道。（本书
后续章节还会介绍更多组件。）调用resolveActivity(Intent, int)方法，我们可以找到匹配
给定Intent任务的activity。flag标志MATCH_DEFAULT_ONLY限定只搜索带CATEGORY_DEFAULT标
志的activity。这和startActivity(Intent)方法类似。
如果搜到目标，它会返回ResolveInfo告诉你找到了哪个activity。如果找不到的话，必须禁
用嫌疑人按钮，否则应用就会崩溃
         */
        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact, PackageManager.MATCH_DEFAULT_ONLY)
                        == null) {
            mSuspectButton.setEnabled(false);
        }

        mPhotoButton = (ImageButton) v.findViewById(R.id.crime_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        boolean canTakePhoto = mPhotoFile != null &&
                captureImage.resolveActivity(packageManager) != null;
        mPhotoButton.setEnabled(canTakePhoto);

        if (canTakePhoto) {
            Uri uri = Uri.fromFile(mPhotoFile);
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }

        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(captureImage, REQUEST_PHOTO);

            }
        });
        //does not work---can't open the camera

        mPhotoView = (ImageView) v.findViewById(R.id.crime_photo);


        return v;

    }


    //添加remove crime 功能
    //只加getActivity().finish(); －－－》 只能按顺序删除，否则会直接退出程序
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_delete_crime:
                CrimeLab.get(getActivity()).deleteCrime(mCrime);
                //getActivity().finish();
                setResult();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
    在CrimeFragment中，覆盖onActivityResult(...)方法，从extra中获取日期数据，设置
    对应Crime的记录日期，然后刷新日期按钮的显示
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();

        } else if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();
            // Specify which fields you want your query to return
            // values for.
            String[] queryFields = new String[] {
                    ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID};
            //Perform your query - the contactUri is like a "where"
            // clause here
            Cursor c = getActivity().getContentResolver().query(contactUri, queryFields, null,
                    null, null);
            try {
                //Double-check that you actually got results
                if (c.getCount() == 0) {
                    return;
                }

                // Pull out the first column of the first row of data-
                // that is your suspect's name.
                c.moveToFirst();
                String suspect = c.getString(0);
                long contactId = c.getLong(1);
                mCrime.setSuspect(suspect);
                mCrime.setContactId(contactId);
                mSuspectButton.setText(suspect);
                updateCallSuspectButton(); //enables the button and changes its text
            }finally {
                c.close();
            }

        }
    }


    /*
    在onCreateView(...)和onActivityResult(...)方法中，设置按钮显示文字的代码完全
    一样。为避免代码冗余，可以将其封装到updateDate()公共方法中，然后分别调用。
    除手工封装公共代码的方式外，还可以使用Android Studio的内置工具。加亮选取设置
    mDateButton显示文字的代码，右键单击并选择Refactor → Extract → Method...菜单项
     */
    private void updateDate() {
        mDateButton.setText(mCrime.getDate().toString());
    }

    public void returnResult() {
        getActivity().setResult(Activity.RESULT_OK, null);

    }

    private void updateCallSuspectButton() {
        mCallSuspectButton.setClickable(true);

    }



    private void setResult() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DELETE_CRIME, true);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

    private String getCrimeReport() {
        String solvedString = null;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);

        }else {
            solvedString = getString(R.string.crime_report_unsolved);
        }
        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);

        }else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }
        String report = getString(R.string.crime_report, mCrime.getTitle(),
                dateString, solvedString, suspect);
        return report;

    }


}
