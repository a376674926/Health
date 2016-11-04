
package cn.stj.fphealth.tcp.mina;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public class CommandDecoder extends CumulativeProtocolDecoder {

    protected boolean doDecode(IoSession session, IoBuffer in,
            ProtocolDecoderOutput out) throws Exception {
        if(in.remaining() >= TcpProtocol.MESSAGE_HEADER_LEN){
            in.mark();
            byte encode = in.get();
            byte encrypt = in.get();
            byte version = in.get();
            byte clientType = in.get();
            byte extend = in.get();
            long sessionId = in.getLong();
            int status = in.getInt();
            int command = in.getInt();

            byte[] token = new byte[TcpProtocol.TOKEN_LENGTH];
            in.get(token);
            String tokenStr = new String(token, 0, 32);
            int dataLength = in.getInt();
            
            int messageRemaining = dataLength + TcpProtocol.END_LENGTH;
            if(messageRemaining > in.remaining()){
              //如果消息内容不够，则重置恢复position位置到操作前,进入下一轮, 接收新数据，以拼凑成完整数据
               in.reset();
               return false;
            }else{
               //消息内容足够
                TcpProtocol tcpProtocol = new TcpProtocol();
                tcpProtocol.setClientType(clientType);
                tcpProtocol.setCommand(command);
                tcpProtocol.setEncode(encode);
                tcpProtocol.setEncrypt(encrypt);
                tcpProtocol.setExtend(extend);
                tcpProtocol.setLength(dataLength);
                tcpProtocol.setSessionId(sessionId);
                tcpProtocol.setStatus(status);
                tcpProtocol.setToken(tokenStr);
                tcpProtocol.setVersion(version);
                byte[] dataBytes = new byte[dataLength];
                in.get(dataBytes);
                tcpProtocol.datasFromBytes(dataBytes);
                byte[] endBytes = new byte[TcpProtocol.END_LENGTH];
                in.get(endBytes);
                String end = new String(endBytes, 0, TcpProtocol.END_LENGTH);
                tcpProtocol.setEnd(end);
                out.write(tcpProtocol);
                if(in.remaining() > 0){//如果读取一个完整包内容后还粘了包，就让父类再调用一次，进行下一次解析
                    return true;
                }
            } 
        }
        return false;//处理成功，让父类进行接收下个包
        
    }

}
