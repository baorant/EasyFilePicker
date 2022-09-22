package com.sky.filepicker.upload

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.sky.filepicker.R
import com.sky.filepicker.model.ChooseFileTypeBean
import com.sky.filepicker.popupwindowlibrary.bean.FiltrateBean
import com.sky.filepicker.popupwindowlibrary.view.ScreenPopWindow
import kotlinx.android.synthetic.main.activity_local_upload.*
import org.greenrobot.eventbus.EventBus

class LocalUpdateActivity : AppCompatActivity(), View.OnClickListener {
    private var viewModel: LocalUpdateActivityViewModel? = null
    private var storageFragment: StorageFragment? = null
    val pathList = ArrayList<String>()
    var maxNum: Int? = null
    private val dictList = ArrayList<FiltrateBean>()
    var screenPopWindow: ScreenPopWindow? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_local_upload)

        maxNum = intent.getIntExtra("maxNum",1)
        tv_back.setOnClickListener(this)
        tv_upload.setOnClickListener(this)
        tv_filter.setOnClickListener(this)
        checkPermission()
        initData()
        screenPopWindow = ScreenPopWindow(this, dictList)
        //设置多选
        screenPopWindow?.setSingle(false)?.build()
    }

    private fun initData() {
        viewModel = ViewModelProviders.of(this).get(LocalUpdateActivityViewModel::class.java)
        val fileTypes = arrayOf("JPGE", "PNG", "GIF", "webp", "jpg", "mp4", "3gp", "mpg", "txt", "pdf", "log")
        /*————————*/
        val filtrateBean = FiltrateBean()
        filtrateBean.typeName = "文件格式"
        val childrenList = ArrayList<FiltrateBean.Children>()
        for (fileType in fileTypes) {
            val cd = FiltrateBean.Children()
            cd.value = fileType
            childrenList.add(cd)
        }
        filtrateBean.children = childrenList
        dictList.add(filtrateBean)
    }

    override fun onClick(v: View?) {
        when (v) {
            tv_back -> finish()
            tv_upload -> upload()
            tv_filter -> filter()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        var isAllAgree = true
        for (i in grantResults.indices) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                isAllAgree = false
            }
        }

        if (!isAllAgree) {
            Toast.makeText(this, "请打开读写手机存储权限", Toast.LENGTH_SHORT).show()
        } else {
            goNext()
        }
    }

    private fun checkPermission() {
        var needReq = false
        for (i in Constants.PERMISSIONS_STORAGE.indices) {
            if (PackageManager.PERMISSION_GRANTED !=
                ContextCompat.checkSelfPermission(
                    this@LocalUpdateActivity,
                    Constants.PERMISSIONS_STORAGE[i]
                )
            ) {
                needReq = true
            }
        }
        if (needReq) {
            ActivityCompat.requestPermissions(
                this,
                Constants.PERMISSIONS_STORAGE,
                Constants.REQUEST_EXTERNAL_STORAGE
            )
        } else {
            goNext()
        }
    }

    private fun goNext() {
        storageFragment =
            StorageFragment.newInstance(Environment.getExternalStorageDirectory().absolutePath)
        storageFragment?.let { storageFragment ->
            supportFragmentManager.beginTransaction().apply {
                add(R.id.fl_container, storageFragment)
                commit()
            }
        }
    }

    private fun upload() {
        if (pathList.size <= 0) {
            Toast.makeText(this, "还未选择文件", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent()
        //此处返回的列表数据
        intent.putExtra("pathList", pathList)
        setResult(Constants.UPLOAD_FILE_RESULT, intent)
        finish()
    }

    private fun filter() {
        screenPopWindow?.showAsDropDown(tv_filter)
        screenPopWindow?.setOnConfirmClickListener { list ->
            viewModel?.chooseFileTypes?.clear()
            list.forEach {fileType -> viewModel?.chooseFileTypes?.add(fileType)}
            // 如果选择筛选的类型>0，图片颜色变红
            if (viewModel?.chooseFileTypes?.size!! > 0) {
                tv_filter.setBackgroundResource(R.drawable.icon_commercial_real_pass);
            } else {
                tv_filter.setBackgroundResource(R.drawable.icon_commercial_real_sort_normal)
            }
            // 点击确认后去更新文件列表
            EventBus.getDefault().post(ChooseFileTypeBean(viewModel?.chooseFileTypes))
        }
        screenPopWindow?.setOnResetClickListener {
            viewModel?.chooseFileTypes?.clear()
            tv_filter.setBackgroundResource(R.drawable.icon_commercial_real_sort_normal)
        }
    }

    fun setText() {
        if (pathList.size > 0) {
            tv_upload.text = "确认(${pathList.size})"
        } else {
            tv_upload.text = "确认"
        }
    }
}