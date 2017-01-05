package com.circuits.circuitsmod.testblock;

import java.util.Optional;

import com.circuits.circuitsmod.common.Log;

import net.minecraft.block.BlockDirectional;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Tester block for puzzle dimension puzzles
 * @author spm61
 *
 */
public class TestingBlock extends BlockDirectional implements ITileEntityProvider {
//IUpdatePlayerListBox
	public TestingBlock() {
		super(Material.IRON);
	    //bubble-07: Puzzle dimension features delayed for future release. It's more important to get the overworld functionality out there for now,
	    //fix bugs, and solicit feedback from redstoners to achieve a good degree of polish while the puzzle dimension levels are completed.
		//this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityTesting();
	}
	
	@SideOnly(Side.CLIENT)
	  public BlockRenderLayer getBlockLayer()
	  {
	    return BlockRenderLayer.SOLID;
	  }

	@Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
		if (!worldIn.isRemote) {
			//Block placed
			TileEntityTesting tileEntity = (TileEntityTesting)worldIn.getTileEntity(pos);
			if (tileEntity != null) {
				Optional<Integer> levelID = TestingItem.getLevelID(stack);
				if (levelID.isPresent()) {
					tileEntity.init(worldIn, levelID.get());
				}
				
				else {
					Log.internalError("Invalid LevelID for the given stack:  " + stack);
				}
			}
		}
    }
	
	 @Override
	  public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		 if (!worldIn.isRemote) {
			 TileEntityTesting tileEntity = (TileEntityTesting)worldIn.getTileEntity(pos);
			 tileEntity.startTest(worldIn);
			 MinecraftServer server = worldIn.getMinecraftServer();
			 PlayerList list = server.getPlayerList();
			 list.sendChatMsg(new TextComponentTranslation("Now testing...Please wait!"));
			 return true;
		 }
		 return true;
	 }
	
	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return true;
	}
	
	/*@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
	{
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing());
	}*/
}
