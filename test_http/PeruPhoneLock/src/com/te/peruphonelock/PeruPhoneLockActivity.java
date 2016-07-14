package com.te.peruphonelock;

import java.lang.reflect.Method;

import org.apache.http.impl.client.TunnelRefusedException;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.provider.Settings;  
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.telephony.TelephonyManager;
import android.provider.Settings;  
import static android.view.WindowManager.LayoutParams.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//import com.te.peruphonelock.TeAnimationWriteNvram;
import com.mediatek.telephony.TelephonyManagerEx;

public class PeruPhoneLockActivity extends Activity {
	private static String TAG = "PeruPhoneLock";
	private static String SUPER_PSWD = "*#11223344#*";
	private static final String SW_FACTORYMODE_TEST_ORDER = "*#37*#";
	private static final String MMI_IMEI_DISPLAY = "*#06#";
	private static final int MAX_TIMES = 5;
	private Context mContext;
	private SharedPreferences mSharedPreferences;
	private SharedPreferences.Editor mEditor;
	
	private boolean isPassed;
	private final String KEYNAME = "REGISTRATION";
	private final String KEYFLAG = "FLAG";
	private final String KEYCONT = "CONT";
	private TextView mSearchText;
	private Button mSearchBtn;
	private TextView mDialogText;
	private Button mCancelBtn;
	private Button mCommitBtn;
	private EditText mEditPannel;
	private String warning_textString = "";
	/*
	 * true  ----- use Pref to save
	 * false ----- use nvram to save state
	 * */
	private boolean usePrefState = false;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"PeruPhoneLockActivity <OnCreate()>");
        mContext = getApplicationContext();
        setFinishOnTouchOutside(false);
        
        mSharedPreferences = mContext.getSharedPreferences(KEYNAME, 0);
		isPassed = mSharedPreferences.getBoolean(KEYFLAG, false);
		
		//1、是否验证通过
		    //1.1是，进入系统
		    //1.2否，进入验证界面
		          //1.21验证界面判断是否还要验证机会
		                 //有，提示验证窗口
		                 //没有，提示一个dialog，重新下版本窗口。
		//debug
		//TeAnimationWriteNvram.te_animation_wirte_bulid_pro_nvram(0);
		int varify = TeAnimationWriteNvram.te_animation_get_bulid_pro_nvram();
		Log.i(TAG,"PeruPhoneLockActivity varify = " + varify);
		Log.i(TAG,"PeruPhoneLockActivity isPassed = " + isPassed);
		
		if(usePrefState ? isPassed : (varify > 0)){
			//if(!isPassed){//isPassed
			//disable the perulock
			disablePeruLock();
			
			Log.i(TAG,"direct --- > GoToHome +++++ ");
			GoToHome(mContext);
		}else{
			setContentView(R.layout.activity_peru_phone_lock);
			initViews();
		}
	}		
        
    public void initViews(){
		String dialogString = "";
		warning_textString = getResources().getString(R.string.te_warning);
		
		mSearchText = (TextView)findViewById(R.id.search_title);
		mSearchBtn = (Button)findViewById(R.id.search_btn);
		
    	mDialogText = (TextView)findViewById(R.id.dialog_title);
    	mEditPannel =(EditText)findViewById(R.id.edit_pannel);
        mCancelBtn = (Button)findViewById(R.id.calcel_btn);
        mCommitBtn = (Button)findViewById(R.id.commit_btn);
        //initContFlags();
        
        mDialogText.setText(R.string.te_input_pswd);	
        
/*        
   		int cont_num = getContValues();
        Log.i(TAG,"NOW  cont_num  = " + cont_num);
        if(cont_num > 0){
        	mDialogText.setText(getResources().getString(R.string.te_input_pswd) 
        			+ " "+ getContValues() + " " 
        			+ getResources().getString(R.string.try_times_count));	
        }else{
        	mCommitBtn.setVisibility(View.GONE);
        	mEditPannel.setVisibility(View.GONE);
        	
        	dialogString = getResources().getString(R.string.max_times_warning);
        	mDialogText.setText(dialogString);
        }
        
        */
        
        mSearchBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mSearchText.setText("IMEI: " + getImeiStr());
			}
		});
        
        mCommitBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//1.判断password 是否正确
				//confim password 
				String inputStr = mEditPannel.getText().toString();
				Log.v(TAG, "inputStr = " + inputStr);
				if (SW_FACTORYMODE_TEST_ORDER.equals(inputStr)) {
				//
					Intent i = new Intent(Intent.ACTION_MAIN);
					i.setClassName("com.mediatek.factorymode",
									"com.mediatek.factorymode.FactoryModeMain");
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					mContext.startActivity(i);
					
				}else if (MMI_IMEI_DISPLAY.equals(inputStr)) {
					String imeiString = "";
					String imeiString1 = (TelephonyManagerEx.getDefault().getDeviceId(0) == null)?"Invalid":TelephonyManagerEx.getDefault().getDeviceId(0);
					//String imeiString2 = (TelephonyManagerEx.getDefault().getDeviceId(1) == null)?"Invalid":TelephonyManagerEx.getDefault().getDeviceId(1);
					imeiString = "SIM 1: " + imeiString1;
					
					Log.v(TAG, "imeiString = " + imeiString);
					showDialogMsg("IMEI",imeiString);
				}else if(confimInput(inputStr)){
					if(!inputStr.equals(SUPER_PSWD)){
						
						String success_toast = getResources().getString(R.string.success_string);
						String ok_Str = getResources().getString(R.string.ok_string);
						
						mCommitBtn.setVisibility(View.GONE);
			        	mEditPannel.setVisibility(View.GONE);
			        	mCancelBtn.setVisibility(View.VISIBLE);
			        	
			        	mDialogText.setText(success_toast);
			        	mCancelBtn.setText(ok_Str);
			        	
			    		Toast.makeText(getApplicationContext(), success_toast, Toast.LENGTH_SHORT).show();
			    		
			        	//set state to 1
			    		if (usePrefState) {
			    			setPassFlag(true);
			    			Log.v(TAG, "pass --->set setPassFlag(true) ---");
						}else {
							TeAnimationWriteNvram.te_animation_wirte_bulid_pro_nvram(1);
				    		Log.v(TAG, "pass --->set TeAnimationWriteNvram from 0 to 1");
						}
			    		//remove app
			    		disablePeruLock();
			    					    		
			    		//go to home
			    		//goBackToHome();
			    		GoToHome(mContext);
					}else {
						//super password
						//go to home 
						Log.v(TAG, "___ spuer pswd pass --->");
						goBackToHome();
					}
				}
				//else{
				//clear edit text
				ClearEditText();
				//}
			}
		});
        
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
					//finishVerify();
			}
		});
    }
    
    private void ClearEditText() {
    	mEditPannel.setText("");
	}
    
    /*
     * get the imei number
     * */
    public String getImeiStr(){
    	String imeiStr = "";
    	imeiStr = TelephonyManagerEx.getDefault().getDeviceId(0);
    	if(imeiStr == null){
    		imeiStr = getResources().getString(R.string.te_invalid_imei);
    	}
    	return imeiStr;	
    }
    
    public boolean confimInput(String inputStr){
    	if(inputStr.equals(GetPhoneLockPasswordByIMEI()) || inputStr.equals(SUPER_PSWD)){
    		return true;	
    	}else if(inputStr.equals("")){
    		String empty_toast = getResources().getString(R.string.empty_string);
    		Toast.makeText(getApplicationContext(), empty_toast, Toast.LENGTH_SHORT).show();
    		return false;
    	}else{
    		if(getContValues()>0){
    			setContValues(getContValues()-1);
    			String error_toast = getResources().getString(R.string.error_string);
        		//Toast.makeText(getApplicationContext(), error_toast, 1).show();
        		showDialogMsg(warning_textString, error_toast);
        		
        		/*mDialogText.setText(
        				getResources().getString(R.string.te_input_pswd) 
        				+ " " + getContValues() 
        				+ " " + getResources().getString(R.string.try_times_count));*/
    		}else{
    			//超过最大次数了，需要提醒用户，拨打1025
    			//mDialogText.setText(R.string.max_times_warning);
    			//mCommitBtn.setEnabled(false);
    			
    			String max_times_warning = getResources().getString(R.string.max_times_warning);
        		//Toast.makeText(getApplicationContext(), suggest_toast, 1).show();
    			showDialogMsg(warning_textString,max_times_warning);
    		}
    		return false;
    	}
    }
    
    /*
     * get the passwrod by imei number
     * *#"IMEI 首位放最后，从第二位开始2位相加取个位"#
     * */
    public String GenT818UnlockSimPasswordFromIMEI() {
		StringBuilder resultString = new StringBuilder();
		String imeiString = "";
		String ReadimeiString = "";
		//char[] imeiarray;
		
		//final TelephonyManager telephonyManager = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
		//mTm = TelephonyManagerEx.getDefault();
		ReadimeiString = TelephonyManagerEx.getDefault().getDeviceId(0);
		Log.d(TAG,"ReadimeiString = " +ReadimeiString);
		Log.d(TAG,"GenT818UnlockSimPassword = " +imeiString);
		if (ReadimeiString==null) {
			ReadimeiString = "321456987456321";
 		Log.d(TAG,"ReadimeiString1=" +ReadimeiString);
		}
	        Log.d(TAG,"ReadimeiString2=" +ReadimeiString);
		if (ReadimeiString!=null) {
			
			imeiString = ReadimeiString.substring(1);
			Log.d(TAG,"ReadimeiString = " +ReadimeiString);
			Log.d(TAG,"imeiString = " +imeiString);
			//imeiarray = imeiString.toCharArray();
			resultString.append("*#");
			for (int i = 0; i < imeiString.length(); i++) {
				if (i+1 >= imeiString.length()) {
					break;
				}				
				char string1 = imeiString.charAt(i);
				char string2 = imeiString.charAt(++i);
				int v1 = Integer.parseInt(String.valueOf(string1));
				int v2 = Integer.parseInt(String.valueOf(string2));
				//int value = Integer.valueOf(string1) + Integer.valueOf(string2);
				int value = v1  + v2;
				if (value >= 10) {
					value -= 10;
				}
				resultString.append(value);
			}
			resultString.append(ReadimeiString.charAt(0));
			resultString.append("#");
		}
			Log.d(TAG,"gxt  GenT818UnlockSimPassword = " + resultString.toString());
		return resultString.toString();
	}	

    /*
     * get the passwrod by imei number
     * IMEI后6位+123456）x 2，取结果后6位
     * */
    
    public String GetPhoneLockPasswordByIMEI() {
    	int tem_pswd = 0;
    	int final_pswd = 0;
    	
		StringBuilder resultString = new StringBuilder();
		String imeiString = "";
		String ReadimeiString = "";
		String RswdString = "";
		//char[] imeiarray;
		
		//final TelephonyManager telephonyManager = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
		//mTm = TelephonyManagerEx.getDefault();
		ReadimeiString = TelephonyManagerEx.getDefault().getDeviceId(0);
		Log.d(TAG,"PhoneLock  ReadimeiString = " + ReadimeiString);
		
		if (ReadimeiString==null) {
			ReadimeiString = "321456987456321";
			Log.d(TAG,"dft ReadimeiString1 = " + ReadimeiString);
		}
		if (ReadimeiString!=null) {
			
			//imeiString = ReadimeiString.substring(1);
			//IMEI后6位
			Log.d(TAG,"sim1 befor cut ReadimeiString = " + ReadimeiString);
			imeiString = ReadimeiString.substring(ReadimeiString.length()-6);
			Log.d(TAG,"sim1 After cut imeiString = " + imeiString);
			
			//imeiarray = imeiString.toCharArray();
			resultString.append("*#");
			//IMEI后6位+123456）x 2，取结果后6位
			//string to int
			tem_pswd = Integer.parseInt(String.valueOf(imeiString));
			Log.d(TAG,"begin tem_pswd = " + tem_pswd);
			//calculate pswd
			final_pswd = (tem_pswd + 123456)*2;
			Log.d(TAG,"caltulate  final_pswd = " + final_pswd);
			//int to string
			RswdString = Integer.toString(final_pswd);
			Log.d(TAG,"caltulate  RswdString = " + RswdString);
			//取结果后6位
			resultString.append(RswdString.substring(RswdString.length()-6));
			resultString.append("#");
		}
		Log.d(TAG," Final  resultString = " + resultString.toString());
		return resultString.toString();
	}
    
    
	public void initContFlags(){
    	mSharedPreferences = getSharedPreferences(KEYNAME, MODE_PRIVATE);
    	mEditor = mSharedPreferences.edit();
    	//此处每次进来需要有一个新的判断fixfixfixfix
    	mEditor.putInt(KEYCONT, MAX_TIMES);
    	mEditor.putBoolean(KEYFLAG, false);
    	mEditor.commit();
    	
    }
	
  //设置验证次数的值
    public void setContValues(int value) {
		mSharedPreferences = getSharedPreferences(KEYNAME, MODE_PRIVATE);
		mEditor = mSharedPreferences.edit();
		mEditor.putInt(KEYCONT, value);
		mEditor.commit();
	}
    //获取验证次数的值
    public int getContValues() {
		mSharedPreferences = getSharedPreferences(KEYNAME, MODE_PRIVATE);
		int value = mSharedPreferences.getInt(KEYCONT, MAX_TIMES);
		return value;
	}
    
    //获取是否验证过的值
    public String getValues(String key) {
    	mSharedPreferences = getSharedPreferences(KEYNAME, MODE_PRIVATE);
		String values = mSharedPreferences.getString(key, "default");
		return values;
    }
    
    //设置通过的标示
    public void setPassFlag(Boolean flag) {
    	mSharedPreferences = getSharedPreferences(KEYNAME, MODE_PRIVATE);
		mEditor = mSharedPreferences.edit();
		mEditor.putBoolean(KEYFLAG, flag);
		mEditor.commit();
    }
    
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	public void disablePeruLock(){
            //guxinting add 05-06
		Log.i(TAG,"---- disablePeruLock()--------");
	        PackageManager pm = getPackageManager();
			ComponentName name = new ComponentName("com.te.peruphonelock", "com.te.peruphonelock.PeruPhoneLockActivity");
	        pm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_DISABLED ,
				PackageManager.DONT_KILL_APP);	
	}
	    
	private void goBackToHome() {
		Log.i(TAG,"---- goto launcher3 ------");
		Intent i = new Intent(Intent.ACTION_MAIN);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setClassName("com.android.launcher3", "com.android.launcher3.Launcher");
        startActivity(i);
       
        finish();
	}
	
	private void GoToHome(Context context) {
		Log.i(TAG,"++++ GoToHome ------");
		Intent i = new Intent(Intent.ACTION_MAIN);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
        i.addCategory(Intent.CATEGORY_HOME);
        context.startActivity(i);
        finish();
	}
	
	private void showDialogMsg(CharSequence title, CharSequence mString) {
		Log.v(TAG, "showDialogMsg ------ ");
		
		AlertDialog mDialog = new AlertDialog.Builder(PeruPhoneLockActivity.this)
										.setTitle(title)
										.setMessage(mString)
										.setPositiveButton(R.string.ok_string, new DialogInterface.OnClickListener() {
											
											@Override
											public void onClick(DialogInterface dialog, int which) {
												// TODO Auto-generated method stub
												//finish();
												dialog.cancel();
											}
										}).create();
		if (!mDialog.isShowing()) {
			mDialog.show();
		}
	}
}
