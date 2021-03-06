package lilypad.client.connect.lib.request.impl;

import io.netty.buffer.ByteBuf;
import lilypad.client.connect.api.request.impl.MessageRequest;
import lilypad.client.connect.lib.request.RequestEncoder;
import lilypad.packet.common.util.BufferUtils;
import lilypad.packet.connect.ConnectPacketConstants;

public class MessageRequestEncoder implements RequestEncoder<MessageRequest> {

	public void encode(MessageRequest request, ByteBuf buffer) {
		buffer.writeShort(request.getRecipients().size());
		for(String username : request.getRecipients()) {
			BufferUtils.writeString(buffer, username);
		}
		BufferUtils.writeString(buffer, request.getChannel());
		buffer.writeShort(request.getMessage().length);
		buffer.writeBytes(request.getMessage());
	}

	public int getId() {
		return ConnectPacketConstants.requestMessage;
	}

	public Class<MessageRequest> getRequest() {
		return MessageRequest.class;
	}

}
