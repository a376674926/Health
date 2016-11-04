
package cn.stj.fphealth.tcp.mina;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class CommandCodecFactory implements ProtocolCodecFactory {

    public CommandCodecFactory() {

    }

    @Override
    public ProtocolDecoder getDecoder(IoSession session) throws Exception {
        // TODO Auto-generated method stub
        return new CommandDecoder();
    }

    @Override
    public ProtocolEncoder getEncoder(IoSession session) throws Exception {
        // TODO Auto-generated method stub
        return new CommandEncoder();
    }

}
