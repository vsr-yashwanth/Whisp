package com.example.offlinechat.ui;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000*\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\u001a$\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0012\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00010\u0005H\u0007\u001a&\u0010\u0006\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00010\fH\u0007\u001a\u001e\u0010\r\u001a\u00020\u00012\u0006\u0010\u000e\u001a\u00020\u00032\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00010\fH\u0007\u00a8\u0006\u0010"}, d2 = {"ChatBubble", "", "msg", "Lcom/example/offlinechat/ChatMessage;", "onInspectRoute", "Lkotlin/Function1;", "ChatScreen", "peerName", "", "viewModel", "Lcom/example/offlinechat/ChatViewModel;", "onNavigateBack", "Lkotlin/Function0;", "RouteInspectorDialog", "message", "onDismiss", "app_debug"})
public final class ChatScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable
    public static final void ChatScreen(@org.jetbrains.annotations.NotNull
    java.lang.String peerName, @org.jetbrains.annotations.NotNull
    com.example.offlinechat.ChatViewModel viewModel, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateBack) {
    }
    
    @androidx.compose.runtime.Composable
    public static final void ChatBubble(@org.jetbrains.annotations.NotNull
    com.example.offlinechat.ChatMessage msg, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.example.offlinechat.ChatMessage, kotlin.Unit> onInspectRoute) {
    }
    
    @androidx.compose.runtime.Composable
    public static final void RouteInspectorDialog(@org.jetbrains.annotations.NotNull
    com.example.offlinechat.ChatMessage message, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
}