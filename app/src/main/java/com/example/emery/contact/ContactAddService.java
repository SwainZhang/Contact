package com.example.emery.contact;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import com.example.emery.contact.constant.Constants;

import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

/**
 * Created by emery on 2017/4/22.
 * <p>
 * 如果一个任务需要分解成多个任务，并且按一定的顺序执行，那么就应该选择IntentService
 */

public class ContactAddService extends IntentService {

    private int mBegin;
    private int mI = 0;


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ContactAddService(String name) {
        super(name);
    }

    public ContactAddService() {
        super("");
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Bundle extras = intent.getExtras();
        mBegin = extras.getInt(Constants.INDEX, 1);
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        AddTask addTask = new AddTask(0, 300L);
        ForkJoinTask<Long> result = forkJoinPool.submit(addTask);

    }

    public void insertBacth(String name, String phone) {
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        ContentResolver resolver = getContentResolver();
        ArrayList<ContentProviderOperation> operations = new ArrayList();
        ContentProviderOperation op1 = ContentProviderOperation.newInsert(uri)
                .withValue("account_name", null)
                .build();
        operations.add(op1);

        uri = Uri.parse("content://com.android.contacts/data");
        //添加姓名
        ContentProviderOperation op2 = ContentProviderOperation.newInsert(uri)
                .withValueBackReference("raw_contact_id", 0)
                .withValue("mimetype", "vnd.android.cursor.item/name")
                .withValue("data2", name)
                .build();
        operations.add(op2);
        //添加电话号码
        ContentProviderOperation op3 = ContentProviderOperation.newInsert(uri)
                .withValueBackReference("raw_contact_id", 0)
                .withValue("mimetype", "vnd.android.cursor.item/phone_v2")
                .withValue("data1", phone)
                .build();
        operations.add(op3);

        try {
            resolver.applyBatch("com.android" + ".contacts", operations);

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public class AddTask extends RecursiveTask<Long> {
        private static final int THRESHOLD = 100;
        private long start;
        private long end;

        public AddTask(long start, long end) {
            this.start = start;
            this.end = end;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected Long compute() {
            long sum = 0;
            boolean canDeal = (end - start) <= THRESHOLD;
            if (canDeal) {
                for (long i = start; i <=end; i++) {
                    insertBacth("联系人一" + i, "" + i);
                    System.out.println("name=联系人二 =" + i + "电话=" + i + "线程id=" + Thread
                            .currentThread
                                    ().getId());
                }

            } else {
                long step = (start + end) / 100;
                ArrayList<AddTask> tasks=new ArrayList<>();
                for (mI = 0; mI < step; mI++) {
                    System.out.println("mi---" + mI);
                    AddTask addTask = new AddTask(mI * 100 + 1, (mI + 1) * 100);
                    tasks.add(addTask);
                    addTask.fork();
                    System.out.println("============");
                }
                for (AddTask task : tasks) {
                    task.join();
                }

            }

            return 0L;
        }
    }

}
