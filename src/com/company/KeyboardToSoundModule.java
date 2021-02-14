package net.snortum.play.midi;

import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.List;

// https://github.com/ksnortum/midi-examples/blob/master/src/main/java/net/snortum/play/midi/MidiDeviceDisplay.java

/**
 * Create a connection between a musical keyboard (transmitter) and an internal
 * synthesizer.  You should first run {@link MidiDeviceDisplay} to discover the
 * device names for each of these.
 *
 * @author Knute Snortum, Modified by Anton Minnie
 * @version 2017/06/17
 */
public class KeyboardToSoundModule {

    /**
     * Name values can have the class name, (see {@link MidiSystem}), the device
     * name or both. Use a pound sign (#) to separate the class and device name.
     * Get device names from the {@link MidiDeviceDisplay} program, or leave
     * empty for default.<p>
     *
     * {@code javax.sound.midi.Transmitter#USB Uno MIDI Interface}<br>
     * {@code javax.sound.midi.Synthesizer#Microsoft MIDI Mapper}<br>
     */
    //private static final String TRANS_DEV_NAME = "javax.sound.midi.Transmitter#USB Uno MIDI Interface";
    //private static final String SYNTH_DEV_NAME = "javax.sound.midi.Synthesizer#Microsoft MIDI Mapper";

    private static final String TRANS_DEV_NAME = "javax.sound.midi.Transmitter#2- Seaboard RISE 49";
    //private static final String SYNTH_DEV_NAME = "javax.sound.midi.Synthesizer#Deebach-Blackbox";
    private static final String SYNTH_DEV_NAME = "javax.sound.midi.Synthesizer#Gervill";
    private static final String SEQ_DEV_NAME = "default";

    /** See {@link MidiSystem} for other classes */
    private static final String TRANS_PROP_KEY = "javax.sound.midi.Transmitter";
    private static final String SYNTH_PROP_KEY = "javax.sound.midi.Synthesizer";
    private static final String SEQ_PROP_KEY = "javax.sound.midi.Sequence";

    Synthesizer synthesizer;
    Sequencer sequencer;
    Receiver midircv;

    private String selindevice = "Seaboard";
    private String seloutdevice = "Deebach-Blackbox";

    // Layered channels out (defaulted): presetIdx, channelInIdx, (ChannelOutIdx & ModuleIdx) * 10, OctaveTran
    private byte[] channelOutStruct = {0, 13, 0, 0, 14, 0, 15, 0, 16, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,};

    class StatusMidiDevice {
        boolean isactive;
        MidiDevice device;

        StatusMidiDevice(MidiDevice device, boolean isactive) {
            this.device = device;
            this.isactive = isactive;

            System.out.println("Adding MIDI Device: " + toString());
        }

        @Override
        public String toString() {
            String devicestring = "Active:" + isactive + " Device:" + device.getDeviceInfo().toString();
            return devicestring;
        }
    }

    final List<StatusMidiDevice> InDeviceList = new ArrayList<>();
    final List<StatusMidiDevice> OutDeviceList = new ArrayList<>();

    public static void main(String[] args) {
        new net.snortum.play.midi.KeyboardToSoundModule().run();
    }

    private void run() {

        // Initialize Input and Output Device Lists
        loadMidiDevices();

        listInDevices();
        listOutDevices();

        try {
            // Get output Synth or external Sound MOdule
            midircv = openMidiReceiver();
            if (midircv == null) {
                return;
            }

            // Get a transmitter and synthesizer from their device names
            // using system properties or defaults
            Transmitter trans = getTransmitter();
            if (trans == null) {
                return;
            }

            // You get your receiver from the synthesizer, then set it in
            // your transmitter.  Optionally, you can create an implementation
            // of Receiver to display the messages before they're sent.
            //trans.setReceiver(midircv); // or just "receiver"
            AMidiFXReceiver displayReceiver = new AMidiFXReceiver(midircv); // optional
            trans.setReceiver(displayReceiver); // or just "receiver"

            // Get default sequencer, if it exists
            sequencer = getSequencer();
            if (sequencer == null) {
                return;
            }

            sequencer.open();
            sequencer.getTransmitter().setReceiver(midircv);

            // You should be able to play on your musical keyboard (transmitter)
            // and hear sounds through your PC synthesizer (receiver)
            System.out.println("Play on your musical keyboard...");

            // Play Demo Sequencer Song in parallel with Keyboard input
            playDemoSequence(1);

            sequencer.close();
        }
        catch (Exception e) {     //// MidiUnavailableException
            System.err.println("Error getting receiver from synthesizer");
            e.printStackTrace();
        }
    }

    // Play Song on Sequencer
    private void playDemoSequence(int replaycnt) {

        for (int i = 0; i < replaycnt; i++) {
            System.out.println("Starting Demo Sequencer Play:" + i);

            sequencer.setTempoInBPM(144.0f);

            // Input MIDI data
            try {
                sequencer.setSequence(getMidiInputData());
            } catch (InvalidMidiDataException e1) {
                e1.printStackTrace();
                return;
            }

            // Play sequence
            // Sleep, or first note is too long
            sleep(200);
            sequencer.start();

            while (sequencer.isRunning()) {
                sleep(1000);
            }

            // Sleep or last note is clipped
            sleep(200);
        }
    }


    /**
     * @return a specific synthesizer object by setting the system property, otherwise the default
     */
    private Synthesizer getSynthesizer() {
        if (! SYNTH_DEV_NAME.isEmpty() || ! "default".equalsIgnoreCase(SYNTH_DEV_NAME)) {
            System.setProperty(SYNTH_PROP_KEY, SYNTH_DEV_NAME);
        }

        try {
            return MidiSystem.getSynthesizer();
        }
        catch (MidiUnavailableException e) {
            System.err.println("Error getting synthesizer");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @return a specific transmitter object by setting the system property, otherwise the default
     */
    private Transmitter getTransmitter() {
        if (! TRANS_DEV_NAME.isEmpty() && ! "default".equalsIgnoreCase(TRANS_DEV_NAME)) {
            System.setProperty(TRANS_PROP_KEY, TRANS_DEV_NAME);
        }

        try {
            return MidiSystem.getTransmitter();
        }
        catch (MidiUnavailableException e) {
            System.err.println("Error getting transmitter");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @return a specific sequencer object by setting the system property,
     *         otherwise the default
     */
    private Sequencer getSequencer() {
        if (!SEQ_DEV_NAME.isEmpty()
                || !"default".equalsIgnoreCase(SEQ_DEV_NAME)) {
            System.setProperty(SEQ_PROP_KEY, SEQ_DEV_NAME);
        }

        try {
            return MidiSystem.getSequencer();
        }
        catch (MidiUnavailableException e) {
            System.err.println("Error getting sequencer");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Implementation of {@link Receiver} that will display the MIDI message
     * before sending it.
     *
     * @author Knute Snortum
     * @version 2017/06/16
     */
    private class AMidiFXReceiver implements Receiver {
        private Receiver receiver;
        boolean isSystemExclusiveData = false;

        public AMidiFXReceiver(Receiver receiver) {
            this.receiver = receiver;
        }

        @Override
        public void send(MidiMessage message, long timeStamp) {
            //receiver.send(message, timeStamp);
            routeMessage(message, timeStamp);

            //displayMessage(message, timeStamp);
        }

        @Override
        public void close() {
            receiver.close();
        }

        // Display MIDI message
        private void routeMessage(MidiMessage message, long timeStamp) {

            //receiver.send(message, timeStamp);

            // Check: Are we printing system exclusive data?
            if (isSystemExclusiveData) {
                receiver.send(message, timeStamp);
                return;
            }

            int status = message.getStatus();

            // Do not route status and timing messages
            if (( status == 0xf8 ) || ( status == 0xfe )) {
                receiver.send(message, timeStamp);
                return;
            }

            //System.out.printf("%d - Status: 0x%s", timeStamp, Integer.toHexString(status));

            // These statuses have MIDI channel numbers and data (except 0xf0 thru 0xff)
            // Strip channel number out of status
            int leftNibble = status & 0xf0;

            switch (leftNibble) {
                case 0x80: //displayNoteOff(message);
                case 0x90: //displayNoteOn(message);
                    //receiver.send(message, timeStamp);
                    layerMessages(message, timeStamp);
                    break;
                case 0xa0: //displayKeyPressure(message);
                case 0xb0: //displayControllerChange(message);
                case 0xc0: //displayProgramChange(message);
                case 0xd0: //displayChannelPressure(message);
                case 0xe0: //displayPitchBend(message);
                case 0xf0:
                    receiver.send(message, timeStamp);
                    //layerMessages(message, timeStamp);
                    break;
                default:
                    // Not recognized, but forward
                    receiver.send(message, timeStamp);
            }
        }

        // Play original note and any layering as needed
        private void layerMessages(MidiMessage message, long timeStamp) {
            ShortMessage shortmessage;

            if (message.getLength() < 3 || message.getLength() % 2 == 0) {
                System.out.println("Unable to Layer Bad MIDI message");
                return;
            }

            // Now dissect to determine if Layering is needed and forward in layered channels
            byte[] bytes = message.getMessage();
            int command = message.getStatus() & 0xf0;
            int channel = message.getStatus() & 0x0f;

            // Layer input layer to output channels
            try {
                // Layer the first/origin Channel
                int chan = channelOutStruct[2];
                if (chan != 0) {
                    shortmessage = new ShortMessage();
                    shortmessage.setMessage(command, chan - 1, byteToInt(bytes[1]) + 4, byteToInt(bytes[2]));
                    receiver.send(shortmessage, timeStamp);

                    System.out.println("Layer Channel index[0]: " + chan);
                }

                // Lookup and layer the remaining channels until a 0 out is found
                int startidx = 4;
                int idx = 0;

                chan = channelOutStruct[startidx];
                if ((chan < 0) || (chan > 16)) return;
                while (chan != 0) {
                    shortmessage = new ShortMessage();
                    shortmessage.setMessage(command, chan - 1, byteToInt(bytes[1]), byteToInt(bytes[2]));
                    receiver.send(shortmessage, timeStamp);

                    // Read next channel mapping
                    int offsetidx = startidx + (idx++ * 2);
                    if (offsetidx > (channelOutStruct.length - 1))
                        break;
                    chan = channelOutStruct[offsetidx];
                    if ((chan <= 0) || (chan > 16)) return;

                    System.out.println("Layer Channel index[" + idx + "]: " + chan);
                }
            }
            catch (InvalidMidiDataException ex) {
                System.out.print("Invalid Channel Layer Message" + channel);
                System.out.print(ex);
            }
        }

        // Display MIDI message
        private void displayMessage(MidiMessage message, long timeStamp) {

            // Check: Are we printing system exclusive data?
            if (isSystemExclusiveData) {
                displayRawData(message);
                return;
            }

            int status = message.getStatus();

            // These statuses clutter the display
            if ( status == 0xf8 ) { return; } // ignore timing messages
            if ( status == 0xfe ) { return; } // ignore status active

            System.out.printf("%d - Status: 0x%s",
                    timeStamp, Integer.toHexString(status));

            // Strip channel number out of status
            int leftNibble = status & 0xf0;

            // These statuses have MIDI channel numbers and data (except
            // 0xf0 thru 0xff)
            switch (leftNibble) {
                case 0x80: displayNoteOff(message); break;
                case 0x90: displayNoteOn(message); break;
                case 0xa0: displayKeyPressure(message); break;
                case 0xb0: displayControllerChange(message); break;
                case 0xc0: displayProgramChange(message); break;
                case 0xd0: displayChannelPressure(message); break;
                case 0xe0: displayPitchBend(message); break;
                case 0xf0: displaySystemMessage(message); break;
                default:
                    System.out.println(" Unknown status");
                    displayRawData(message);
            }
        }

        // Displays raw data as integers, if any
        private void displayRawData(MidiMessage message) {
            byte[] bytes = message.getMessage();

            if (message.getLength() > 1) {
                System.out.print("\tRaw data: ");

                for (int i = 1; i < bytes.length; i++) {
                    System.out.print(byteToInt(bytes[i]) + " ");
                }

                System.out.println();
            }
        }

        // Display status and data of a NoteOn message.  Data may come
        // in pairs after the status byte.
        //
        // Note that a NoteOn with a velocity of 0 is synonymous with
        // a NoteOff message.
        private void displayNoteOn(MidiMessage message) {
            if (message.getLength() < 3 || message.getLength() % 2 == 0) {
                System.out.println(" Bad MIDI message");
                return;
            }

            byte[] bytes = message.getMessage();

            // Zero velocity
            if ( bytes[2] == 0 ) {
                System.out.print(" = Note off");
            } else {
                System.out.print(" = Note on");
            }

            System.out.print(", Channel " + midiChannelToInt(message));

            if ( bytes[2] == 0 ) {
                System.out.println(", Note " + byteToInt(bytes[1]));
                return;
            }

            System.out.print("\n\t");

            for (int i = 1; i < message.getLength(); i += 2) {
                if ( i > 1 ) {
                    System.out.print("; ");
                }
                System.out.printf( "Number %d, Velocity %d",
                        byteToInt(bytes[i]) , byteToInt(bytes[i + 1]) );
            }

            System.out.println();
        }

        // Display status and data of a NoteOff message.
        private void displayNoteOff(MidiMessage message) {
            if (message.getLength() < 3 || message.getLength() % 2 == 0) {
                System.out.println(" Bad MIDI message");
            }
            else {
                byte[] bytes = message.getMessage();
                System.out.printf(" = Note off, Channel %d, Note %d%n",
                        midiChannelToInt(message), byteToInt(bytes[1]));
                System.out.println();
            }
        }

        // Display status and data of a ControllerChange message.  Data may come
        // in pairs after the status byte.
        private void displayControllerChange(MidiMessage message) {
            if (message.getLength() < 3 || message.getLength() % 2 == 0) {
                System.out.println(" Bad MIDI message");
                return;
            }

            System.out.print(" = Controller Change, Channel "
                    + midiChannelToInt(message) + "\n\t");

            byte[] bytes = message.getMessage();
            for (int i = 1; i < message.getLength(); i += 2) {
                if ( i > 1 ) {
                    System.out.print("; ");
                }
                System.out.printf( "Controller %d, Value %d",
                        byteToInt(bytes[i]), byteToInt(bytes[i + 1]) );
            }

            System.out.println();
        }

        // Display status and data of a KeyPressure message.  Data may come
        // in pairs after the status byte.
        private void displayKeyPressure(MidiMessage message) {
            if (message.getLength() < 3 || message.getLength() % 2 == 0) {
                System.out.println(" Bad MIDI message");
                return;
            }

            System.out.print(" = Key Pressure, Channel "
                    + midiChannelToInt(message) + "\n\t");

            byte[] bytes = message.getMessage();
            for (int i = 1; i < message.getLength(); i += 2) {
                if ( i > 1 ) {
                    System.out.print("; ");
                }
                System.out.printf( "Note Number %d, Pressure %d",
                        byteToInt(bytes[i]), byteToInt(bytes[i + 1]) );
            }

            System.out.println();
        }

        // Display status and data of a PitchBend message.  Data may come
        // in pairs after the status byte.
        private void displayPitchBend(MidiMessage message) {
            if (message.getLength() < 3 || message.getLength() % 2 == 0) {
                System.out.println(" Bad MIDI message");
                return;
            }

            System.out.print(" = Pitch Bend, Channel "
                    + midiChannelToInt(message) + "\n\t");

            byte[] bytes = message.getMessage();
            for (int i = 1; i < message.getLength(); i += 2) {
                if ( i > 1 ) {
                    System.out.print("; ");
                }
                System.out.printf( "Value %d",
                        bytesToInt(bytes[i], bytes[i + 1]) );
            }

            System.out.println();
        }

        // Display status and data of a ProgramChange message
        private void displayProgramChange(MidiMessage message) {
            if (message.getLength() < 2) {
                System.out.println(" Bad MIDI message");
                return;
            }

            System.out.print(" = Program Change, Channel "
                    + midiChannelToInt(message) + "\n\t");

            byte[] bytes = message.getMessage();
            for (int i = 1; i < message.getLength(); i++) {
                if ( i > 1 ) {
                    System.out.print(", ");
                }
                System.out.println("Program Number " + byteToInt(bytes[i]));
            }
        }

        // Display status and data of a ChannelPressure message
        private void displayChannelPressure(MidiMessage message) {
            if (message.getLength() < 2) {
                System.out.println(" Bad MIDI message");
                return;
            }

            System.out.print(" = Channel Pressure, Channel "
                    + midiChannelToInt(message) + "\n\t");

            byte[] bytes = message.getMessage();
            for (int i = 1; i < message.getLength(); i++) {
                if ( i > 1 ) {
                    System.out.print(", ");
                }
                System.out.println("Pressure " + byteToInt(bytes[i]));
            }
        }

        // Display system messages.  Some may have data.
        //
        // "Begin System Exclusive" stops data interpretation, "End of
        // System Exclusive" starts it again
        private void displaySystemMessage(MidiMessage message) {
            byte[] bytes = message.getMessage();

            switch (message.getStatus()) {
                case 0xf0:
                    System.out.println(" = Begin System Exclusive");
                    isSystemExclusiveData = true;
                    break;
                case 0xf1:
                    if (bytes.length < 2) {
                        System.out.println(" Bad Data");
                    } else {
                        System.out.println(" = MIDI Time Code 1/4 Frame, Time Code "
                                + byteToInt(bytes[1]));
                    }
                    break;
                case 0xf2:
                    if (bytes.length < 3) {
                        System.out.println(" Bad Data");
                    } else {
                        System.out.println(" = Song Position, Pointer "
                                + bytesToInt(bytes[1], bytes[2]));
                    }
                case 0xf3:
                    if (bytes.length < 2) {
                        System.out.println(" Bad Data");
                    } else {
                        System.out.println(" = Song Select, Song "
                                + byteToInt(bytes[1]));
                    }
                    break;
                case 0xf6:
                    System.out.println(" = Tune Request");
                    break;
                case 0xf7:
                    System.out.println(" = End of System Exclusive");
                    isSystemExclusiveData = false;
                    break;
                case 0xf8:
                    System.out.println(" = Timing Clock"); // ignored
                    break;
                case 0xfa:
                    System.out.println(" = Start");
                    break;
                case 0xfb:
                    System.out.println(" = Continue");
                    break;
                case 0xfc:
                    System.out.println(" = Stop");
                    break;
                case 0xfe:
                    System.out.println(" = Active Sensing"); // ignored
                    break;
                case 0xff:
                    System.out.println(" = System Reset");
                    break;
                default:
                    System.out.println(" Unknow System Message");
                    displayRawData(message);
            }
        }

        private int byteToInt(byte b) {
            return b & 0xff;
        }

        // Two 7-bit bytes
        private int bytesToInt(byte msb, byte lsb) {
            return byteToInt(msb) * 128 + byteToInt(lsb);
        }

        private int midiChannelToInt(MidiMessage message) {
            return (message.getStatus() & 0x0f) + 1;
        }
    }

    // Check if at least one MIDI (port) device is correctly installed
    public Receiver openMidiReceiver() {
        Receiver midircv = null;
        MidiDevice selectedDevice;

        System.out.println("**********************");
        System.out.println("** openMidireceiver **");

        try {
            selectedDevice = MidiSystem.getSynthesizer();
            MidiDevice.Info[] devices = MidiSystem.getMidiDeviceInfo();

            if (devices.length == 0) {
                System.err.println("### Error: No MIDI devices found");
                return midircv;
            } else {
                boolean binit = true;

                // Loop through all devices looking to Synth or Sound Modules
                for (MidiDevice.Info dev : devices) {
                    if (MidiSystem.getMidiDevice(dev).getMaxReceivers() == 0) {
                        continue;
                    }
                    if (binit) {
                        // Default to first device and override with preferred
                        selectedDevice = MidiSystem.getMidiDevice(dev);
                        binit = false;
                    }

                    System.out.println("Found MIDI Device " + dev.getName());

                    //if (dev.getName().contains("Gervill") && dev instanceof Synthesizer)  {
                    if ( dev.getName().contains(seloutdevice) ) {
                        selectedDevice = MidiSystem.getMidiDevice(dev);

                        System.out.println("Selected MIDI Device Info: " + dev.toString());
                        break;
                    }
                }
            }

            if (!selectedDevice.isOpen()) {
                try {
                    selectedDevice.open();

                    System.out.println("Selected MIDI device *** " + selectedDevice.getDeviceInfo().getName() + " ***");
                }
                catch (MidiUnavailableException e) {
                    System.err.println("Error selecting MIDI device " + e);
                    return midircv;
                }

                // Found output Device or Synth
                midircv = selectedDevice.getReceiver();
            }
        } catch (MidiUnavailableException ex) {
            System.err.println("### Error: Could not open MIDI synthesizer: " + ex);
        }

        return midircv;
    }

    // Create a sequence and set all MIDI events
    private Sequence getMidiInputData() {
        int ticksPerQuarterNote = 4;
        Sequence seq;
        try {
            seq = new Sequence(Sequence.PPQ, ticksPerQuarterNote);
            setMidiEvents(seq.createTrack());
        }
        catch (InvalidMidiDataException e) {
            e.printStackTrace();
            return null;
        }
        return seq;
    }

    // Set MIDI events to play "Mary Had a Little Lamb"
    private void setMidiEvents(Track track) {
        int channel = 0;
        int velocity = 64;
        int note = 61;
        int tick = 0;
        addMidiEvent(track, ShortMessage.NOTE_ON, channel, note, velocity, tick);
        addMidiEvent(track, ShortMessage.NOTE_OFF, channel, note, 0, tick + 3);
        addMidiEvent(track, ShortMessage.NOTE_ON, channel, note - 2, velocity, tick + 4);
        addMidiEvent(track, ShortMessage.NOTE_OFF, channel, note - 2, 0, tick + 7);
        addMidiEvent(track, ShortMessage.NOTE_ON, channel, note - 4, velocity, tick + 8);
        addMidiEvent(track, ShortMessage.NOTE_OFF, channel, note - 4, 0, tick + 11);
        addMidiEvent(track, ShortMessage.NOTE_ON, channel, note - 2, velocity, tick + 12);
        addMidiEvent(track, ShortMessage.NOTE_OFF, channel, note - 2, 0, tick + 15);
        addMidiEvent(track, ShortMessage.NOTE_ON, channel, note, velocity, tick + 16);
        addMidiEvent(track, ShortMessage.NOTE_OFF, channel, note, 0, tick + 19);
        addMidiEvent(track, ShortMessage.NOTE_ON, channel, note, velocity, tick + 20);
        addMidiEvent(track, ShortMessage.NOTE_OFF, channel, note, 0, tick + 23);
        addMidiEvent(track, ShortMessage.NOTE_ON, channel, note, velocity, tick + 24);
        addMidiEvent(track, ShortMessage.NOTE_OFF, channel, note, 0, tick + 31);
    }

    // Create a MIDI event and add it to the track
    private void addMidiEvent(Track track, int command, int channel, int data1,
                              int data2, int tick) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(command, channel, data1, data2);
        }
        catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
        track.add(new MidiEvent(message, tick));
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // List all Midi Devices detected
    private void loadMidiDevices() {
        MidiDevice.Info[] deviceInfo = MidiSystem.getMidiDeviceInfo();
        if (deviceInfo.length == 0) {
            System.out.println("No MIDI devices found");
            return;
        }

        for (MidiDevice.Info info : deviceInfo) {
            System.out.println("**********************");
            System.out.println("Device name: " + info.getName());
            System.out.println("Description: " + info.getDescription());
            System.out.println("Vendor: " + info.getVendor());
            System.out.println("Version: " + info.getVersion());

            try {
                MidiDevice device = MidiSystem.getMidiDevice(info);
                addDeviceType(device);
                System.out.println("Maximum receivers: "
                        + maxToString(device.getMaxReceivers()));
                System.out.println("Maximum transmitters: "
                        + maxToString(device.getMaxTransmitters()));
            } catch (MidiUnavailableException e) {
                System.out.println("Can't get MIDI device");
                e.printStackTrace();
            }
        }
    }

    private void addDeviceType(MidiDevice device) {
        if (device instanceof Sequencer) {
            System.out.println("This is a sequencer");
            InDeviceList.add(new StatusMidiDevice(device, false));
        }
        else if (device instanceof Synthesizer) {
            System.out.println("This is a synthesizer");
            OutDeviceList.add(new StatusMidiDevice(device, false));
        }
        else {
            System.out.print("This is a MIDI port ");
            if (device.getMaxReceivers() != 0) {
                System.out.println("IN ");

                boolean isactive = false;
                if ( device.getDeviceInfo().getName().contains(selindevice) ) {
                    isactive = true;
                }
                InDeviceList.add(new StatusMidiDevice(device, isactive));
            }
            if (device.getMaxTransmitters() != 0) {
                System.out.println("OUT ");

                boolean isactive = false;
                if ( device.getDeviceInfo().getName().contains(seloutdevice) ) {
                    isactive = true;
                }
                OutDeviceList.add(new StatusMidiDevice(device, isactive));
            }
        }
    }

    private String maxToString(int max) {
        return max == -1 ? "Unlimited" : String.valueOf(max);
    }

    public void listInDevices() {
        System.out.println("**********************");
        for (StatusMidiDevice statusdevice : InDeviceList ) {
            System.out.println("MIDI In Device:" + statusdevice.toString());
        }
    }

    public void listOutDevices() {
        System.out.println("**********************");
        for (StatusMidiDevice statusdevice : OutDeviceList ) {
            System.out.println("MIDI Out Device:" + statusdevice.toString());
        }
    }

    private int mapChannelOut(int chanin) {
        int chanout = 0;

        // Read start channel. It is a sequence of up to 10 channels, starting with PresetIdx at position 0
        int firstchan = channelOutStruct[1];
        chanout = channelOutStruct[1 + (firstchan - chanin) * 2];

        return chanout;
    }

    public byte[] getChannelOut() {
        return channelOutStruct;
    }
}