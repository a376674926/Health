package cn.stj.fphealth.tcp.mina;

import cn.stj.fphealth.util.LogUtil;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class CommandEncoder extends ProtocolEncoderAdapter {

    public void encode(IoSession session, Object message,          
            ProtocolEncoderOutput out) throws Exception {     
        TcpProtocol tcpProtocol = (TcpProtocol) message;          
        byte[] bytes = tcpProtocol.messageToBytes();          
        IoBuffer buf = IoBuffer.allocate(bytes.length, false);                            
        buf.setAutoExpand(true);           
        buf.put(bytes);                            
        buf.flip();           
        out.write(buf);     
    }

}
