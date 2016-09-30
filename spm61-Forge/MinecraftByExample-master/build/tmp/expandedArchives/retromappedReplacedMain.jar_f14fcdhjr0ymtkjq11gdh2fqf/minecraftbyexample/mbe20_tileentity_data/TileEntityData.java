package minecraftbyexample.mbe20_tileentity_data;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.BlockTNT;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Random;

/**
 * User: brandon3055
 * Date: 06/01/2015
 *
 * This is a simple tile entity which stores some data
 * When placed, it waits for 10 seconds then replaces itself with a random block
 */
public class TileEntityData extends TileEntity implements ITickable {

	private final int INVALID_VALUE = -1;
	private int ticksLeftTillDisappear = INVALID_VALUE;  // the time (in ticks) left until the block disappears

	// set by the block upon creation
	public void setTicksLeftTillDisappear(int ticks)
	{
		ticksLeftTillDisappear = ticks;
	}

	// When the world loads from disk, the server needs to send the TileEntity information to the client
	//  it uses getUpdatePacket(), getUpdateTag(), onDataPacket(), and handleUpdateTag() to do this:
  //  getUpdatePacket() and onDataPacket() are used for one-at-a-time TileEntity updates
  //  getUpdateTag() and handleUpdateTag() are used by vanilla to collate together into a single chunk update packet
	//  Not really required for this example since we only use the timer on the client, but included anyway for illustration
	@Override
  @Nullable
  public SPacketUpdateTileEntity func_189518_D_()
  {
		NBTTagCompound nbtTagCompound = new NBTTagCompound();
		func_189515_b(nbtTagCompound);
		int metadata = func_145832_p();
		return new SPacketUpdateTileEntity(this.field_174879_c, metadata, nbtTagCompound);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		func_145839_a(pkt.func_148857_g());
	}

  /* Creates a tag containing the TileEntity information, used by vanilla to transmit from server to client
 */
  @Override
  public NBTTagCompound func_189517_E_()
  {
    NBTTagCompound nbtTagCompound = new NBTTagCompound();
    func_189515_b(nbtTagCompound);
    return nbtTagCompound;
  }

  /* Populates this TileEntity with information from the tag, used by vanilla to transmit from server to client
 */
  @Override
  public void handleUpdateTag(NBTTagCompound tag)
  {
    this.func_145839_a(tag);
  }

  // This is where you save any data that you don't want to lose when the tile entity unloads
	// In this case, we only need to store the ticks left until explosion, but we store a bunch of other
	//  data as well to serve as an example.
	// NBTexplorer is a very useful tool to examine the structure of your NBT saved data and make sure it's correct:
	//   http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-tools/1262665-nbtexplorer-nbt-editor-for-windows-and-mac
	@Override
	public NBTTagCompound func_189515_b(NBTTagCompound parentNBTTagCompound)
	{
		super.func_189515_b(parentNBTTagCompound); // The super call is required to save the tiles location

		parentNBTTagCompound.func_74768_a("ticksLeft", ticksLeftTillDisappear);
		// alternatively - could use parentNBTTagCompound.setTag("ticksLeft", new NBTTagInt(ticksLeftTillDisappear));

		// some examples of other NBT tags - browse NBTTagCompound or search for the subclasses of NBTBase for more examples

		parentNBTTagCompound.func_74778_a("testString", testString);

		NBTTagCompound blockPosNBT = new NBTTagCompound();        // NBTTagCompound is similar to a Java HashMap
		blockPosNBT.func_74768_a("x", testBlockPos.func_177958_n());
		blockPosNBT.func_74768_a("y", testBlockPos.func_177956_o());
		blockPosNBT.func_74768_a("z", testBlockPos.func_177952_p());
		parentNBTTagCompound.func_74782_a("testBlockPos", blockPosNBT);

		NBTTagCompound itemStackNBT = new NBTTagCompound();
		testItemStack.func_77955_b(itemStackNBT);                     // make sure testItemStack is not null first!
		parentNBTTagCompound.func_74782_a("testItemStack", itemStackNBT);

		parentNBTTagCompound.func_74783_a("testIntArray", testIntArray);

		NBTTagList doubleArrayNBT = new NBTTagList();                     // an NBTTagList is similar to a Java ArrayList
		for (double value : testDoubleArray) {
			doubleArrayNBT.func_74742_a(new NBTTagDouble(value));
		}
		parentNBTTagCompound.func_74782_a("testDoubleArray", doubleArrayNBT);

		NBTTagList doubleArrayWithNullsNBT = new NBTTagList();
		for (int i = 0; i < testDoubleArrayWithNulls.length; ++i) {
			Double value = testDoubleArrayWithNulls[i];
			if (value != null) {
				NBTTagCompound dataForThisSlot = new NBTTagCompound();
				dataForThisSlot.func_74768_a("i", i+1);   // avoid using 0, so the default when reading a missing value (0) is obviously invalid
				dataForThisSlot.func_74780_a("v", value);
				doubleArrayWithNullsNBT.func_74742_a(dataForThisSlot);
			}
		}
		parentNBTTagCompound.func_74782_a("testDoubleArrayWithNulls", doubleArrayWithNullsNBT);
    return parentNBTTagCompound;
	}

	// This is where you load the data that you saved in writeToNBT
	@Override
	public void func_145839_a(NBTTagCompound parentNBTTagCompound)
	{
		super.func_145839_a(parentNBTTagCompound); // The super call is required to load the tiles location

		// important rule: never trust the data you read from NBT, make sure it can't cause a crash

		final int NBT_INT_ID = 3;					// see NBTBase.createNewByType()
		int readTicks = INVALID_VALUE;
		if (parentNBTTagCompound.func_150297_b("ticksLeft", NBT_INT_ID)) {  // check if the key exists and is an Int. You can omit this if a default value of 0 is ok.
			readTicks = parentNBTTagCompound.func_74762_e("ticksLeft");
			if (readTicks < 0) readTicks = INVALID_VALUE;
		}
		ticksLeftTillDisappear = readTicks;

		// some examples of other NBT tags - browse NBTTagCompound or search for the subclasses of NBTBase for more

		String readTestString = null;
		final int NBT_STRING_ID = 8;          // see NBTBase.createNewByType()
		if (parentNBTTagCompound.func_150297_b("testString", NBT_STRING_ID)) {
			readTestString = parentNBTTagCompound.func_74779_i("testString");
		}
		if (!testString.equals(readTestString)) {
			System.err.println("testString mismatch:" + readTestString);
		}

		NBTTagCompound blockPosNBT = parentNBTTagCompound.func_74775_l("testBlockPos");
		BlockPos readBlockPos = null;
		if (blockPosNBT.func_150297_b("x", NBT_INT_ID) && blockPosNBT.func_150297_b("y", NBT_INT_ID) && blockPosNBT.func_150297_b("z", NBT_INT_ID) ) {
			readBlockPos = new BlockPos(blockPosNBT.func_74762_e("x"), blockPosNBT.func_74762_e("y"), blockPosNBT.func_74762_e("z"));
		}
		if (readBlockPos == null || !testBlockPos.equals(readBlockPos)) {
			System.err.println("testBlockPos mismatch:" + readBlockPos);
		}

		NBTTagCompound itemStackNBT = parentNBTTagCompound.func_74775_l("testItemStack");
		ItemStack readItemStack = ItemStack.func_77949_a(itemStackNBT);
		if (!ItemStack.func_77989_b(testItemStack, readItemStack)) {
			System.err.println("testItemStack mismatch:" + readItemStack);
		}

		int [] readIntArray = parentNBTTagCompound.func_74759_k("testIntArray");
		if (!Arrays.equals(testIntArray, readIntArray)) {
			System.err.println("testIntArray mismatch:" + readIntArray);
		}

		final int NBT_DOUBLE_ID = 6;					// see NBTBase.createNewByType()
		NBTTagList doubleArrayNBT = parentNBTTagCompound.func_150295_c("testDoubleArray", NBT_DOUBLE_ID);
		int numberOfEntries = Math.min(doubleArrayNBT.func_74745_c(), testDoubleArray.length);
		double [] readDoubleArray = new double[numberOfEntries];
		for (int i = 0; i < numberOfEntries; ++i) {
			 readDoubleArray[i] = doubleArrayNBT.func_150309_d(i);
		}
		if (doubleArrayNBT.func_74745_c() != numberOfEntries || !Arrays.equals(readDoubleArray, testDoubleArray)) {
			System.err.println("testDoubleArray mismatch:" + readDoubleArray);
		}

		final int NBT_COMPOUND_ID = 10;					// see NBTBase.createNewByType()
		NBTTagList doubleNullArrayNBT = parentNBTTagCompound.func_150295_c("testDoubleArrayWithNulls", NBT_COMPOUND_ID);
		numberOfEntries = Math.min(doubleArrayNBT.func_74745_c(), testDoubleArrayWithNulls.length);
		Double [] readDoubleNullArray = new Double[numberOfEntries];
		for (int i = 0; i < doubleNullArrayNBT.func_74745_c(); ++i)	{
			NBTTagCompound nbtEntry = doubleNullArrayNBT.func_150305_b(i);
			int idx = nbtEntry.func_74762_e("i") - 1;
			if (nbtEntry.func_150297_b("v", NBT_DOUBLE_ID) && idx >= 0 && idx < numberOfEntries) {
				readDoubleNullArray[idx] = nbtEntry.func_74769_h("v");
			}
		}
		if (!Arrays.equals(testDoubleArrayWithNulls, readDoubleNullArray)) {
			System.err.println("testDoubleArrayWithNulls mismatch:" + readDoubleNullArray);
		}
	}

	// Since our TileEntity implements ITickable, we get an update method which is called once per tick (20 times / second)
	// When the timer elapses, replace our block with a random one.
	@Override
	public void func_73660_a() {
		if (!this.func_145830_o()) return;  // prevent crash
		World world = this.func_145831_w();
		if (world.field_72995_K) return;   // don't bother doing anything on the client side.
		if (ticksLeftTillDisappear == INVALID_VALUE) return;  // do nothing until the time is valid
		--ticksLeftTillDisappear;
//		this.markDirty();            // if you update a tileentity variable on the server and this should be communicated to the client,
// 																		you need to markDirty() to force a resend.  In this case, the client doesn't need to know
		if (ticksLeftTillDisappear > 0) return;   // not ready yet

		Block [] blockChoices = {Blocks.field_150484_ah, Blocks.field_150343_Z, Blocks.field_150350_a, Blocks.field_150335_W, Blocks.field_150327_N, Blocks.field_150345_g, Blocks.field_150355_j};
		Random random = new Random();
		Block chosenBlock = blockChoices[random.nextInt(blockChoices.length)];
	  world.func_175656_a(this.field_174879_c, chosenBlock.func_176223_P());
		if (chosenBlock == Blocks.field_150335_W) {
			Blocks.field_150335_W.func_176206_d(world, field_174879_c, Blocks.field_150335_W.func_176223_P().func_177226_a(BlockTNT.field_176246_a, true));
			world.func_175698_g(field_174879_c);
		} else if (chosenBlock == Blocks.field_150345_g) {
			BlockSapling blockSapling = (BlockSapling)Blocks.field_150345_g;
			blockSapling.func_176476_e(world, this.field_174879_c, blockSapling.func_176223_P(),random);
		}
	}

	private final int [] testIntArray = {5, 4, 3, 2, 1};
	private final double [] testDoubleArray = {1, 2, 3, 4, 5, 6};
	private final Double [] testDoubleArrayWithNulls = {61.1, 62.2, null, 64.4, 65.5};
	private final ItemStack testItemStack = new ItemStack(Items.field_151077_bg, 23);
	private final String testString = "supermouse";
	private final BlockPos testBlockPos = new BlockPos(10, 11, 12);
}
