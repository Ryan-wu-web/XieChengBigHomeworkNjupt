package com.yisu.app.ai.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.yisu.app.R;
import com.yisu.app.ai.network.AIChatService;
import com.yisu.app.ai.voice.VoiceManager;
import com.yisu.app.model.Result;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class AIChatDialog extends Dialog {
    private Context context;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private Button btnSend;
    private ImageButton btnVoice;
    private MessageAdapter adapter;
    private List<Message> messageList;
    private AIChatService aiChatService;
    private VoiceManager voiceManager;
    private boolean isRecording = false;

    public AIChatDialog(Context context) {
        super(context, android.R.style.Theme_DeviceDefault_Light_Dialog);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_ai_chat);
        initViews();
        initData();
    }

    private void initViews() {
        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnVoice = findViewById(R.id.btnVoice);

        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.CENTER;
            window.setAttributes(params);
        }
    }

    private void initData() {
        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList);
        rvMessages.setLayoutManager(new LinearLayoutManager(context));
        rvMessages.setAdapter(adapter);

        aiChatService = AIChatService.getInstance();
        voiceManager = new VoiceManager(context);

        Message welcomeMessage = new Message();
        welcomeMessage.setRole("ai");
        welcomeMessage.setContent("您好！我是酒店预订助手，有什么可以帮您的吗？");
        messageList.add(welcomeMessage);
        adapter.notifyDataSetChanged();

        btnSend.setOnClickListener(v -> sendMessage());

        btnVoice.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startVoiceRecording();
                    break;
                case MotionEvent.ACTION_UP:
                    stopVoiceRecording();
                    break;
            }
            return true;
        });
    }

    private void sendMessage() {
        String message = etMessage.getText().toString().trim();
        if (message.isEmpty()) return;

        Message userMessage = new Message();
        userMessage.setRole("user");
        userMessage.setContent(message);
        messageList.add(userMessage);
        adapter.notifyDataSetChanged();
        rvMessages.scrollToPosition(messageList.size() - 1);

        etMessage.setText("");

        Message loadingMessage = new Message();
        loadingMessage.setRole("ai");
        loadingMessage.setContent("");
        loadingMessage.setLoading(true);
        messageList.add(loadingMessage);
        adapter.notifyDataSetChanged();
        rvMessages.scrollToPosition(messageList.size() - 1);

        AIChatService.AIChatRequest request = new AIChatService.AIChatRequest();
        request.setMessage(message);
        request.setHotelQuery(true);

        aiChatService.chat(request).enqueue(new Callback<Result<String>>() {
            @Override
            public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Result<String> result = response.body();
                    if ("200".equals(result.getCode())) {
                        messageList.remove(messageList.size() - 1);
                        Message aiMessage = new Message();
                        aiMessage.setRole("ai");
                        aiMessage.setContent(result.getData());
                        messageList.add(aiMessage);
                        adapter.notifyDataSetChanged();
                        rvMessages.scrollToPosition(messageList.size() - 1);
                    }
                }
            }

            @Override
            public void onFailure(Call<Result<String>> call, Throwable t) {
                messageList.remove(messageList.size() - 1);
                Message errorMessage = new Message();
                errorMessage.setRole("ai");
                errorMessage.setContent("抱歉，网络错误，请稍后再试。");
                messageList.add(errorMessage);
                adapter.notifyDataSetChanged();
                rvMessages.scrollToPosition(messageList.size() - 1);
            }
        });
    }

    private void startVoiceRecording() {
        isRecording = true;
        btnVoice.setImageResource(android.R.drawable.ic_media_pause);
        voiceManager.startRecording();
    }

    private void stopVoiceRecording() {
        isRecording = false;
        btnVoice.setImageResource(android.R.drawable.ic_btn_speak_now);
        voiceManager.stopRecording(new VoiceManager.VoiceCallback() {
            @Override
            public void onSuccess(String text) {
                etMessage.setText(text);
                sendMessage();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(context, "语音识别失败: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<Message> messages;

        public MessageAdapter(List<Message> messages) {
            this.messages = messages;
        }

        @Override
        public int getItemViewType(int position) {
            Message message = messages.get(position);
            if (message.isLoading()) {
                return 2;
            }
            return "user".equals(message.getRole()) ? 0 : 1;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == 0) {
                return new UserViewHolder(inflater.inflate(R.layout.item_message_user, parent, false));
            } else if (viewType == 1) {
                return new AIVH(inflater.inflate(R.layout.item_message_ai, parent, false));
            } else {
                return new LoadingVH(inflater.inflate(R.layout.item_message_loading, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            Message message = messages.get(position);
            if (holder instanceof UserViewHolder) {
                ((UserViewHolder) holder).tvContent.setText(message.getContent());
            } else if (holder instanceof AIVH) {
                ((AIVH) holder).tvContent.setText(message.getContent());
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        class UserViewHolder extends RecyclerView.ViewHolder {
            android.widget.TextView tvContent;

            public UserViewHolder(View itemView) {
                super(itemView);
                tvContent = itemView.findViewById(R.id.tvContent);
            }
        }

        class AIVH extends RecyclerView.ViewHolder {
            android.widget.TextView tvContent;

            public AIVH(View itemView) {
                super(itemView);
                tvContent = itemView.findViewById(R.id.tvContent);
            }
        }

        class LoadingVH extends RecyclerView.ViewHolder {
            android.widget.ProgressBar pbLoading;

            public LoadingVH(View itemView) {
                super(itemView);
                pbLoading = itemView.findViewById(R.id.pbLoading);
            }
        }
    }

    private class Message {
        private String role;
        private String content;
        private boolean loading;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public boolean isLoading() {
            return loading;
        }

        public void setLoading(boolean loading) {
            this.loading = loading;
        }
    }
}