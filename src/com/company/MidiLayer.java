package com.company;

/** MIDILayer is a subset of the MIDI Preset data to be shared with the ARM MIDI Controller on:
 * 1. Preset selection from the AMIDIFX UI:
 * 1a: Initial selection of Preset 1: forwards all 8 * 16 layer messages to thr ARM controller
 * 1b: On subsequent preset selections, forwards a delta of the changes to the ARM controller
 * 2: From the AMDIDFX UI, the user may select layer on Upper 1 & 2 & 3, or Lower 1 & 2
 * 3: An output mapping of channel = 0 mutes the channel. Note all channels are index 1 based to enable a muting ndicator of 0
 *
 * Note: The output channel string as stored in the preset file is parsed into a byte array structure to enable quicker resolution in the ARM
 * controller during note play
 */

public class MidiLayer {
    private int presetIdx;
    private int channelIdx;
    private String channelOutIdx;
    private int octaveTran;
    private int moduleIdx;
    private int patchIdx;

    // Layered channels out (defaulted): presetIdx, channelInIdx, (ChannelOutIdx & ModuleIdx) * 10, OctaveTran
    private byte[] channelOutStruct = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,};

    public MidiLayer() {

        this.presetIdx = 0;
        this.channelIdx = 13;
        this.channelOutIdx = "13,0,14,0,15,0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0";
        this.octaveTran = 0;
        this.moduleIdx = 0;
        this.patchIdx = 0;

        parseChannelOut();
    }

    // 0,12,12,0,106,121,100,0,100,100,20,0,0,0,0,3,8,Klaus sein Sax
    public MidiLayer(int presetIdx, int channelIdx, String channelOutIdx, int octaveTran,
                     int moduleIdx, int patchIdx) {

        this.presetIdx = presetIdx;
        this.channelIdx = channelIdx;
        this.channelOutIdx = channelOutIdx;
        this.octaveTran = octaveTran;
        this.moduleIdx = moduleIdx;
        this.patchIdx = patchIdx;

        parseChannelOut();
    }

    public int getPresetIdx() {
        return presetIdx;
    }
    public void setPresetIdx(int presetIdx) {
        this.presetIdx = presetIdx;
    }

    public int getChannelIdx() {
        return channelIdx;
    }
    public void setChannelIdx(int channelIdx) {
        this.channelIdx = channelIdx;
    }

    public String getChannelOutIdx() {
        return channelOutIdx;
    }
    public void setChannelOutIdx(String channelOutIdx) {
        this.channelOutIdx = channelOutIdx;
    }

    public int getOctaveTran() {
        return octaveTran;
    }
    public void setOctaveTran(int octaveTran) {
        this.octaveTran = octaveTran;
    }

    public int getModuleIdx() {
        return moduleIdx;
    }
    public void setModuleIdx(int moduleIdx) {
        this.moduleIdx = moduleIdx;
    }

    public int getPatchIdx() {
        return patchIdx;
    }
    public void setPatchIdx(int patchIdx) {
        this.patchIdx = patchIdx;
    }


    // Parse Channel Out String into Byte Array to be shared with ARM Controller
    // To do: In future, modify the Preset file structure to allow moduleIdx to be spcified at the Channel Out level
    // in order to allow for multiplexing input channels to multiple output modules
    private boolean parseChannelOut() {

        channelOutStruct[0] = (byte)(presetIdx & 0xFF);
        channelOutStruct[1] = (byte)(channelIdx & 0xFF);

        // Convert the channel out string from preset into bytes
        byte[] outchannels = channelOutIdx.getBytes();

        int i = 0, j = 2;
        while (i < outchannels.length) {
            // Skip out channel separator
            if (outchannels[i] == '|') {
                i++;
            }
            channelOutStruct[j] = (byte)(moduleIdx & 0xFF);
            channelOutStruct[j+1] = (byte)(outchannels[i++] - 48);
            //System.out.print("moduleIdx: " + channelOutStruct[j] +  ", channelOutIdx: " + channelOutStruct[j+1]);

            j = j + 2;
        }
        //System.out.println(" <- Channel out byte array");

        return true;
    }


    public byte[] getChannelOut() {
        return channelOutStruct;
    }


    @Override
    public String toString() {
        return "Preset String = [presetIdx=" + presetIdx + ", channelIdx=" + channelIdx
                + ", channelOutIdx=" + channelOutIdx + ", moduleIdx=" + moduleIdx
                + ", patchIdx=" + patchIdx + "]";
    }
}