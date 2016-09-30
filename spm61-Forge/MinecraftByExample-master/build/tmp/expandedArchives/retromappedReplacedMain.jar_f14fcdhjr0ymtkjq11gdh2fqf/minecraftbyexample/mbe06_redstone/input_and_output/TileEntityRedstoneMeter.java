package minecraftbyexample.mbe06_redstone.input_and_output;

import minecraftbyexample.usefultools.UsefulFunctions;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * This TileEntity is used for two main purposes:
 *  1) on the client side - retrieve the current power level for rendering in the associated TileEntitySpecialRenderer (TESR)
 *  2) on the server side
 *     a) used to store the current power level.  This is necessary due to the way that the redstone signals propagate,
 *        e.g. getWeakPower() must retrieve a stored value and not calculate it from neighbours.
 *        see here for more information http://greyminecraftcoder.blogspot.com.au/2015/11/redstone.html
 *     b) used to flash the output at a defined rate using block tick scheduling.
 */
public class TileEntityRedstoneMeter extends TileEntity {

  //  --- client side methods used by the renderer

  // Retrieve the current power level of the meter - the maximum of the four sides (don't look up or down)
	public int getPowerLevelClient() {

//    int powerLevel = this.worldObj.isBlockIndirectlyGettingPowered(this.pos);  // if input can come from any side, use this line

    int maxPowerFound = 0;
    for (EnumFacing whichFace : EnumFacing.field_176754_o) {
      BlockPos neighborPos = field_174879_c.func_177972_a(whichFace);
      int powerLevel = this.field_145850_b.func_175651_c(neighborPos, whichFace);
      maxPowerFound = Math.max(powerLevel, maxPowerFound);
    }
    return maxPowerFound;
  }

  // return the smoothed position of the needle, based on the power level
  public double getSmoothedNeedlePosition()
  {
    int newPowerLevel = getPowerLevelClient();
    if (newPowerLevel != lastPowerLevel) {
      lastPowerLevel = newPowerLevel;
      double targetNeedlePosition = getPowerLevelClient() / 15.0;
      smoothNeedleMovement.setTargetNeedlePosition(targetNeedlePosition, false);
    }

    return smoothNeedleMovement.getSmoothedNeedlePosition();
  }

  private final double NEEDLE_ACCELERATION = 0.4; // acceleration in units per square second
  private final double NEEDLE_MAX_SPEED = 0.4;    // maximum needle movement speed in units per second
  private SmoothNeedleMovement smoothNeedleMovement = new SmoothNeedleMovement(NEEDLE_ACCELERATION, NEEDLE_MAX_SPEED);
  private int lastPowerLevel = -1;

  // -------- server side methods used to keep track of the current power level and alter the output signal state

  public boolean getOutputState()
  {
    return scheduledTogglingOutput.isOn();
  }

  /** whenever a scheduled block update occurs, call this method
   *
   */
  public void onScheduledUpdateTick()
  {
    scheduledTogglingOutput.onUpdateTick(this.func_145831_w(), this.func_174877_v(), this.func_145838_q());
  }

   /**
   *  Change the stored power level (and alters the flashing rate of the power output)
   */
  public void setPowerLevel(int newPowerLevel)
  {
    if (newPowerLevel == storedPowerLevel) return;
    storedPowerLevel = newPowerLevel;
    if (newPowerLevel == 0) {   // always off
      scheduledTogglingOutput.setSteadyOutput(false);
    } else if (newPowerLevel == 15) { // always on
      scheduledTogglingOutput.setSteadyOutput(true);
    } else {
          // flashing: slowest = 1 seconds in 4 seconds; fastest = 0.25 seconds in 0.5 seconds.
      final int LOWEST_POWER = 1;
      final int HIGHEST_POWER = 14;
      final int SLOWEST_ON_TIME = 20; // ticks
      final int FASTEST_ON_TIME = 5; // ticks
      final int SLOWEST_PERIOD = 80; // ticks
      final int FASTEST_PERIOD = 10;  // ticks
      int periodTicks = (int)UsefulFunctions.interpolate(newPowerLevel, LOWEST_POWER, HIGHEST_POWER, SLOWEST_PERIOD, FASTEST_PERIOD);
      int onTicks = (int) UsefulFunctions
              .interpolate(newPowerLevel, LOWEST_POWER, HIGHEST_POWER, SLOWEST_ON_TIME, FASTEST_ON_TIME);
      scheduledTogglingOutput.setToggleRate(this.func_145831_w(), this.func_174877_v(), this.func_145838_q(), onTicks, periodTicks);
    }
  }

  private ScheduledTogglingOutput scheduledTogglingOutput = new ScheduledTogglingOutput();

  private int storedPowerLevel;


  //---------- general TileEntity methods
  // When the world loads from disk, the server needs to send the TileEntity information to the client
  //  it uses getUpdatePacket(), getUpdateTag(), onDataPacket(), and handleUpdateTag() to do this
  // The tag information is loaded and saved using writeToNBT and readFromNBT.
  // In this case, the power level is recalculated every tick on the client anyway, so we don't need to send anything,
  //   but we do need to store the power level on the server, to allow for proper calculation of the redstone power
  //  The update packet methods are shown here for information, even though they're not needed for this example.

  // This is where you save any data that you don't want to lose when the tile entity unloads
  @Override
  public NBTTagCompound func_189515_b(NBTTagCompound parentNBTTagCompound)
  {
    super.func_189515_b(parentNBTTagCompound); // The super call is required to save the tiles location
    parentNBTTagCompound.func_74768_a("storedPowerLevel", storedPowerLevel);
    return parentNBTTagCompound;
  }

  // This is where you load the data that you saved in writeToNBT
  @Override
  public void func_145839_a(NBTTagCompound parentNBTTagCompound)
  {
    super.func_145839_a(parentNBTTagCompound); // The super call is required to load the tiles location
    storedPowerLevel = parentNBTTagCompound.func_74762_e("storedPowerLevel");  // defaults to 0 if not found
    if (storedPowerLevel < 0 ) storedPowerLevel = 0;
    if (storedPowerLevel > 15 ) storedPowerLevel = 15;
  }

  @Override
  @Nullable
  public SPacketUpdateTileEntity func_189518_D_()
  {
    NBTTagCompound updateTagDescribingTileEntityState = func_189517_E_();
    int metadata = func_145832_p();
    return new SPacketUpdateTileEntity(this.field_174879_c, metadata, updateTagDescribingTileEntityState);
  }

  @Override
  public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
    NBTTagCompound updateTagDescribingTileEntityState = pkt.func_148857_g();
    handleUpdateTag(updateTagDescribingTileEntityState);
  }

  /* Creates a tag containing the TileEntity information, used by vanilla to transmit from server to client
     Warning - although our getUpdatePacket() uses this method, vanilla also calls it directly, so don't remove it.
   */
  @Override
  public NBTTagCompound func_189517_E_()
  {
    NBTTagCompound nbtTagCompound = new NBTTagCompound();
    func_189515_b(nbtTagCompound);
    return nbtTagCompound;
  }

  /* Populates this TileEntity with information from the tag, used by vanilla to transmit from server to client
   Warning - although our onDataPacket() uses this method, vanilla also calls it directly, so don't remove it.
 */
  @Override
  public void handleUpdateTag(NBTTagCompound tag)
  {
    this.func_145839_a(tag);
  }

	/**
	 * Don't render the needle if the player is too far away
	 * @return the maximum distance squared at which the TESR should render
	 */
	@SideOnly(Side.CLIENT)
	@Override
	public double func_145833_n()
	{
		final int MAXIMUM_DISTANCE_IN_BLOCKS = 32;
		return MAXIMUM_DISTANCE_IN_BLOCKS * MAXIMUM_DISTANCE_IN_BLOCKS;
	}

	/** Return an appropriate bounding box enclosing the TESR
	 * This method is used to control whether the TESR should be rendered or not, depending on where the player is looking.
	 * The default is the AABB for the parent block, which might be too small if the TESR renders outside the borders of the
	 *   parent block.
	 * If you get the boundary too small, the TESR may disappear when you aren't looking directly at it.
	 * @return an appropriately size AABB for the TileEntity
	 */
	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		// if your render should always be performed regardless of where the player is looking, use infinite
		AxisAlignedBB infiniteExample = INFINITE_EXTENT_AABB;

		// our needles are all on the block faces so our bounding box is from [x,y,z] to  [x+1, y+1, z+1]
		AxisAlignedBB aabb = new AxisAlignedBB(func_174877_v(), func_174877_v().func_177982_a(1, 1, 1));
		return aabb;
	}

}
