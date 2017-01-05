package com.circuits.circuitsmod.controlblock.gui.net;

import java.io.Serializable;

import com.circuits.circuitsmod.recorder.CircuitRecording;

public class ServerGuiMessage implements Serializable {
	private static final long serialVersionUID = 1L;
	private final ServerGuiMessage.GuiMessageKind messageKind;
	private final Serializable data;
	
	public ServerGuiMessage(ServerGuiMessage.GuiMessageKind msgKind) {
		this(msgKind, null);
	}
	
	public ServerGuiMessage(ServerGuiMessage.GuiMessageKind msgKind, Serializable data) {
		this.messageKind = msgKind;
		this.data = data;
	}
	
	public ServerGuiMessage.GuiMessageKind getMessageKind() {
		return this.messageKind;
	}
	
	public Serializable getData() {
		return this.data;
	}
	
	public enum GuiMessageKind implements Serializable {
		GUI_SPECIALIZATON_INFO, GUI_CIRCUIT_COSTS, GUI_COMPILATION_RESULT, GUI_RECORDING_DATA
	};
	
	public static class RecordingData implements Serializable {
		private static final long serialVersionUID = 1L;
		private CircuitRecording recording;
		public RecordingData(CircuitRecording recording) {
			this.recording = recording;
		}
		public CircuitRecording getRecording() {
			return this.recording;
		}
	}
	
	public static class CompilationResult implements Serializable {
		private static final long serialVersionUID = 1L;
		private boolean success;
		public CompilationResult(boolean isSuccessful) {
			this.success = isSuccessful;
		}
		public boolean isSuccess() {
			return this.success;
		}
	}
	
	public static class SpecializationInfo implements Serializable {
		private static final long serialVersionUID = 1L;
		private String specializedName;
		private boolean isSlowable;
		public SpecializationInfo(String specializedName, boolean isSlowable) {
			this.specializedName = specializedName;
			this.isSlowable = isSlowable;
		}
		public String getName() {
			return this.specializedName;
		}
		public boolean isSlowable() {
			return this.isSlowable;
		}
	}
}