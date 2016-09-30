package minecraftbyexample.mbe50_particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * User: The Grey Ghost
 * Date: 24/12/2014
 * Custom Particle to illustrate how to add a Particle with your own texture and movement/animation behaviour
 */
public class FlameParticle extends Particle
{
  private final ResourceLocation flameRL = new ResourceLocation("minecraftbyexample:entity/flame_fx");

  /**
   * Construct a new FlameParticle at the given [x,y,z] position with the given initial velocity.
   */
  public FlameParticle(World world, double x, double y, double z,
                       double velocityX, double velocityY, double velocityZ)
  {
    super(world, x, y, z, velocityX, velocityY, velocityZ);

    field_70545_g = Blocks.field_150480_ab.field_149763_I;  /// arbitrary block!  not required here since we have
    // overriden onUpdate()
    field_70547_e = 100; // not used since we have overridden onUpdate

    final float ALPHA_VALUE = 0.99F;
    this.field_82339_as = ALPHA_VALUE;  // a value less than 1 turns on alpha blending. Otherwise, alpha blending is off
    // and the particle won't be transparent.

    //the vanilla Particle constructor added random variation to our starting velocity.  Undo it!
    field_187129_i = velocityX;
    field_187130_j = velocityY;
    field_187131_k = velocityZ;

    // set the texture to the flame texture, which we have previously added using TextureStitchEvent
    //   (see TextureStitcherBreathFX)
    TextureAtlasSprite sprite = Minecraft.func_71410_x().func_147117_R().func_110572_b(flameRL.toString());
    func_187117_a(sprite);  // initialise the icon to our custom texture
  }

  /**
   * Used to control what texture and lighting is used for the EntityFX.
   * Returns 1, which means "use a texture from the blocks + items texture sheet"
   * The vanilla layers are:
   * normal particles: ignores world brightness lighting map
   *   Layer 0 - uses the particles texture sheet (textures\particle\particles.png)
   *   Layer 1 - uses the blocks + items texture sheet
   * lit particles: changes brightness depending on world lighting i.e. block light + sky light
   *   Layer 3 - uses the blocks + items texture sheet (I think)
   *
   * @return
   */
  @Override
  public int func_70537_b()
  {
    return 1;
  }

  // can be used to change the brightness of the rendered Particle.
  @Override
  public int func_189214_a(float partialTick)
  {
    final int FULL_BRIGHTNESS_VALUE = 0xf000f0;
    return FULL_BRIGHTNESS_VALUE;

    // if you want the brightness to be the local illumination (from block light and sky light) you can just use
    //  Entity.getBrightnessForRender() base method, which contains:
    //    BlockPos blockpos = new BlockPos(this.posX, this.posY, this.posZ);
    //    return this.worldObj.isBlockLoaded(blockpos) ? this.worldObj.getCombinedLight(blockpos, 0) : 0;
  }

  // this function is used by ParticleManager.addEffect() to determine whether depthmask writing should be on or not.
  // FlameBreathFX uses alphablending (i.e. the FX is partially transparent) but we want depthmask writing on,
  //   otherwise translucent objects (such as water) render over the top of our breath, even if the breath is in front
  //  of the water and not behind
  @Override
  public boolean func_187111_c()
  {
    return false;
  }

  /**
   * call once per tick to update the Particle position, calculate collisions, remove when max lifetime is reached, etc
   */
  @Override
  public void func_189213_a()
  {
    field_187123_c = field_187126_f;
    field_187124_d = field_187127_g;
    field_187125_e = field_187128_h;

    func_187110_a(field_187129_i, field_187130_j, field_187131_k);  // simple linear motion.  You can change speed by changing motionX, motionY,
      // motionZ every tick.  For example - you can make the particle accelerate downwards due to gravity by
      // final double GRAVITY_ACCELERATION_PER_TICK = -0.02;
      // motionY += GRAVITY_ACCELERATION_PER_TICK;

    // collision with a block makes the ball disappear.  But does not collide with entities
    if (field_187132_l) {
      this.func_187112_i();
    }

    if (this.field_70547_e-- <= 0) {
      this.func_187112_i();
    }
  }

  /**
   * Render the Particle onto the screen.  For more help with the tessellator see
   * http://greyminecraftcoder.blogspot.co.at/2014/12/the-tessellator-and-worldrenderer-18.html
   * <p/>
   * You don't actually need to override this method, this is just a deobfuscated example of the vanilla, to show you
   * how it works in case you want to do something a bit unusual.
   * <p/>
   * The Particle is rendered as a two-dimensional object (Quad) in the world (three-dimensional coordinates).
   * The corners of the quad are chosen so that the Particle is drawn directly facing the viewer (or in other words,
   * so that the quad is always directly face-on to the screen.)
   * In order to manage this, it needs to know two direction vectors:
   * 1) the 3D vector direction corresponding to left-right on the viewer's screen (edgeLRdirection)
   * 2) the 3D vector direction corresponding to up-down on the viewer's screen (edgeUDdirection)
   * These two vectors are calculated by the caller.
   * For example, the top right corner of the quad on the viewer's screen is equal to:
   * the centre point of the quad (x,y,z)
   * plus the edgeLRdirection vector multiplied by half the quad's width
   * plus the edgeUDdirection vector multiplied by half the quad's height.
   * NB edgeLRdirectionY is not provided because it's always 0, i.e. the top of the viewer's screen is always directly
   * up, so moving left-right on the viewer's screen doesn't affect the y coordinate position in the world
   *
   * @param vertexBuffer
   * @param entity
   * @param partialTick
   * @param edgeLRdirectionX edgeLRdirection[XYZ] is the vector direction pointing left-right on the player's screen
   * @param edgeUDdirectionY edgeUDdirection[XYZ] is the vector direction pointing up-down on the player's screen
   * @param edgeLRdirectionZ edgeLRdirection[XYZ] is the vector direction pointing left-right on the player's screen
   * @param edgeUDdirectionX edgeUDdirection[XYZ] is the vector direction pointing up-down on the player's screen
   * @param edgeUDdirectionZ edgeUDdirection[XYZ] is the vector direction pointing up-down on the player's screen
   */
  @Override
  public void func_180434_a(VertexBuffer vertexBuffer, Entity entity, float partialTick,
                            float edgeLRdirectionX, float edgeUDdirectionY, float edgeLRdirectionZ,
                            float edgeUDdirectionX, float edgeUDdirectionZ)
  {
    double minU = this.field_187119_C.func_94209_e();
    double maxU = this.field_187119_C.func_94212_f();
    double minV = this.field_187119_C.func_94206_g();
    double maxV = this.field_187119_C.func_94210_h();

    double scale = 0.1F * this.field_70544_f;  // vanilla scaling factor
    final double scaleLR = scale;
    final double scaleUD = scale;
    double x = this.field_187123_c + (this.field_187126_f - this.field_187123_c) * partialTick - field_70556_an;
    double y = this.field_187124_d + (this.field_187127_g - this.field_187124_d) * partialTick - field_70554_ao;
    double z = this.field_187125_e + (this.field_187128_h - this.field_187125_e) * partialTick - field_70555_ap;


    // "lightmap" changes the brightness of the particle depending on the local illumination (block light, sky light)
    //  in this example, it's held constant, but we still need to add it to each vertex anyway.
    int combinedBrightness = this.func_189214_a(partialTick);
    int skyLightTimes16 = combinedBrightness >> 16 & 65535;
    int blockLightTimes16 = combinedBrightness & 65535;

    // the caller has already initiated rendering, using:
//    worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);

    vertexBuffer.func_181662_b(x - edgeLRdirectionX * scaleLR - edgeUDdirectionX * scaleUD,
            y - edgeUDdirectionY * scaleUD,
            z - edgeLRdirectionZ * scaleLR - edgeUDdirectionZ * scaleUD)
                 .func_187315_a(maxU, maxV)
                 .func_181666_a(this.field_70552_h, this.field_70553_i, this.field_70551_j, this.field_82339_as)
                 .func_187314_a(skyLightTimes16, blockLightTimes16)
                 .func_181675_d();
    vertexBuffer.func_181662_b(x - edgeLRdirectionX * scaleLR + edgeUDdirectionX * scaleUD,
            y + edgeUDdirectionY * scaleUD,
            z - edgeLRdirectionZ * scaleLR + edgeUDdirectionZ * scaleUD)
            .func_187315_a(maxU, minV)
            .func_181666_a(this.field_70552_h, this.field_70553_i, this.field_70551_j, this.field_82339_as)
            .func_187314_a(skyLightTimes16, blockLightTimes16)
            .func_181675_d();
    vertexBuffer.func_181662_b(x + edgeLRdirectionX * scaleLR + edgeUDdirectionX * scaleUD,
            y + edgeUDdirectionY * scaleUD,
            z + edgeLRdirectionZ * scaleLR + edgeUDdirectionZ * scaleUD)
            .func_187315_a(minU, minV)
            .func_181666_a(this.field_70552_h, this.field_70553_i, this.field_70551_j, this.field_82339_as)
            .func_187314_a(skyLightTimes16, blockLightTimes16)
            .func_181675_d();
    vertexBuffer.func_181662_b(x + edgeLRdirectionX * scaleLR - edgeUDdirectionX * scaleUD,
            y - edgeUDdirectionY * scaleUD,
            z + edgeLRdirectionZ * scaleLR - edgeUDdirectionZ * scaleUD)
            .func_187315_a(minU, maxV)
            .func_181666_a(this.field_70552_h, this.field_70553_i, this.field_70551_j, this.field_82339_as)
            .func_187314_a(skyLightTimes16, blockLightTimes16)
            .func_181675_d();

  }

}
