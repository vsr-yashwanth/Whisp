package com.example.offlinechat.ui;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000R\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0007\n\u0000\u001a\u0016\u0010\u0000\u001a\u00020\u00012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00010\u0003H\u0007\u001a*\u0010\u0004\u001a\u00020\u00012\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\b2\b\b\u0002\u0010\n\u001a\u00020\u000bH\u0007\u001a\u001e\u0010\f\u001a\u00020\u00012\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000e2\u0006\u0010\u0010\u001a\u00020\u0011H\u0007\u001a \u0010\u0012\u001a\u00020\u00012\u0006\u0010\u0013\u001a\u00020\u000f2\u000e\b\u0002\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00010\u0003H\u0007\u001a\u0018\u0010\u0015\u001a\u00020\u00012\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u0019H\u0007\u001a\u001e\u0010\u001a\u001a\u00020\u00012\u0006\u0010\u0013\u001a\u00020\u000f2\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00010\u0003H\u0007\u001a\u0018\u0010\u001c\u001a\u00020\u00012\u0006\u0010\u001d\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020 H\u0007\u00a8\u0006!"}, d2 = {"AdminScreen", "", "onNavigateBack", "Lkotlin/Function0;", "MetricChip", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "title", "", "value", "modifier", "Landroidx/compose/ui/Modifier;", "NetworkRadarCard", "peers", "", "Lcom/example/offlinechat/network/Peer;", "isGlobal", "", "NodeItemCard", "peer", "onClick", "PacketTraceCard", "msg", "Lcom/example/offlinechat/data/Message;", "cryptoManager", "Lcom/example/offlinechat/security/CryptoManager;", "PeerDetailsDialog", "onDismiss", "RouteCandidateCard", "candidate", "Lcom/example/offlinechat/routing/RouteCandidate;", "score", "", "app_debug"})
public final class AdminScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable
    public static final void AdminScreen(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateBack) {
    }
    
    @androidx.compose.runtime.Composable
    public static final void RouteCandidateCard(@org.jetbrains.annotations.NotNull
    com.example.offlinechat.routing.RouteCandidate candidate, float score) {
    }
    
    @androidx.compose.runtime.Composable
    public static final void NetworkRadarCard(@org.jetbrains.annotations.NotNull
    java.util.List<com.example.offlinechat.network.Peer> peers, boolean isGlobal) {
    }
    
    @androidx.compose.runtime.Composable
    public static final void MetricChip(@org.jetbrains.annotations.NotNull
    androidx.compose.ui.graphics.vector.ImageVector icon, @org.jetbrains.annotations.NotNull
    java.lang.String title, @org.jetbrains.annotations.NotNull
    java.lang.String value, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    public static final void NodeItemCard(@org.jetbrains.annotations.NotNull
    com.example.offlinechat.network.Peer peer, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable
    public static final void PacketTraceCard(@org.jetbrains.annotations.NotNull
    com.example.offlinechat.data.Message msg, @org.jetbrains.annotations.NotNull
    com.example.offlinechat.security.CryptoManager cryptoManager) {
    }
    
    @androidx.compose.runtime.Composable
    public static final void PeerDetailsDialog(@org.jetbrains.annotations.NotNull
    com.example.offlinechat.network.Peer peer, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
}