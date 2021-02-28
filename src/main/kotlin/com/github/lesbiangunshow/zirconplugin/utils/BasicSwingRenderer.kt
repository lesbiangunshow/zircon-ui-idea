package com.github.lesbiangunshow.zirconplugin.utils

import org.hexworks.cobalt.databinding.api.extension.toProperty
import org.hexworks.zircon.api.behavior.TilesetHolder
import org.hexworks.zircon.api.data.CharacterTile
import org.hexworks.zircon.api.data.Position
import org.hexworks.zircon.api.data.StackedTile
import org.hexworks.zircon.api.data.Tile
import org.hexworks.zircon.api.graphics.TileGraphics
import org.hexworks.zircon.api.modifier.TileTransformModifier
import org.hexworks.zircon.api.resource.TilesetResource
import org.hexworks.zircon.api.tileset.Tileset
import org.hexworks.zircon.internal.graphics.FastTileGraphics
import org.hexworks.zircon.internal.grid.InternalTileGrid
import org.hexworks.zircon.internal.renderer.Renderer
import org.hexworks.zircon.internal.tileset.SwingTilesetLoader
import org.hexworks.zircon.platform.util.SystemUtils
import java.awt.*
import java.awt.Canvas
import java.awt.Color
import java.awt.image.BufferStrategy
import javax.swing.JPanel

class BasicCanvasRenderer(
    private val panel: JPanel,
    private val tileGrid: InternalTileGrid,
    private val canvas: Canvas = Canvas(),
) : Renderer {

    override val isClosed = false.toProperty()

    private var lastRender: Long = SystemUtils.getCurrentTimeMs()
    private val gridPositions = tileGrid.size.fetchPositions().toList()
    private val tilesetLoader = SwingTilesetLoader()

    override fun close() {
        tileGrid.close()
    }

    override fun create() {
        panel.add(canvas)
        panel.isEnabled = true
        panel.isVisible = true


        canvas.preferredSize = Dimension(
            tileGrid.widthInPixels,
            tileGrid.heightInPixels
        )
        canvas.minimumSize = Dimension(tileGrid.tileset.width, tileGrid.tileset.height)
        canvas.isFocusable = false
        canvas.requestFocusInWindow()
        canvas.createBufferStrategy(2)
        initializeBufferStrategy()
    }

    override fun render() {
        val now = SystemUtils.getCurrentTimeMs()

        tileGrid.updateAnimations(now, tileGrid)
        val bs: BufferStrategy = canvas.bufferStrategy // this is a regular Swing Canvas object
        canvas.bufferStrategy.drawGraphics
        canvas.bufferStrategy.drawGraphics.configure().apply {
            color = Color.BLACK
            fillRect(0, 0, tileGrid.widthInPixels, tileGrid.heightInPixels)
            drawTiles(this)

            dispose()
        }

        bs.show()
        lastRender = now
    }

    private fun Tile.tiles(): List<Tile> = if (this is StackedTile) {
        tiles.flatMap { it.tiles() }
    } else listOf(this)

    private fun fetchLayers(): List<Pair<Position, TileGraphics>> {
        return tileGrid.renderables.map { renderable ->
            val tg = FastTileGraphics(
                initialSize = renderable.size,
                initialTileset = renderable.tileset,
                initialTiles = emptyMap()
            )
            if (!renderable.isHidden) {
                renderable.render(tg)
            }
            renderable.position to tg
        }
    }

    private fun renderTile(
        graphics: Graphics2D,
        position: Position,
        tile: Tile,
        tileset: Tileset<Graphics2D>
    ) {
        if (tile.isNotEmpty) {
            var finalTile = tile
            finalTile.modifiers.filterIsInstance<TileTransformModifier<CharacterTile>>().forEach { modifier ->
                if (modifier.canTransform(finalTile)) {
                    (finalTile as? CharacterTile)?.let {
                        finalTile = modifier.transform(it)
                    }
                }
            }
            finalTile = tile
            ((finalTile as? TilesetHolder)?.let {
                tilesetLoader.loadTilesetFrom(it.tileset)
            } ?: tileset).drawTile(
                tile = finalTile,
                surface = graphics,
                position = position
            )
        }
    }

    private fun drawTiles(graphics: Graphics2D) {
        val layers = fetchLayers()
        val tiles = mutableListOf<Pair<Tile, TilesetResource>>()
        gridPositions.forEach { pos ->
            tiles@ for (i in layers.size - 1 downTo 0) {
                val (layerPos, layer) = layers[i]
                val toRender = layer.getTileAtOrNull(pos - layerPos)?.tiles() ?: listOf()
                for (j in toRender.size - 1 downTo 0) {
                    val tile = toRender[j]
                    tiles.add(0, tile to tile.finalTileset(layer))
                    if (tile.isOpaque) {
                        break@tiles
                    }
                }

            }
            for ((tile, tileset) in tiles) {
                renderTile(
                    graphics = graphics,
                    position = pos,
                    tile = tile,
                    tileset = tilesetLoader.loadTilesetFrom(tileset)
                )
            }
            tiles.clear()
        }
    }

    private fun Graphics.configure(): Graphics2D {
        val gc = this as Graphics2D
        gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF)
        gc.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED)
        gc.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE)
        gc.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF)
        gc.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF)
        gc.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED)
        gc.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY)
        return gc
    }

    private tailrec fun initializeBufferStrategy() {
        val bs = canvas.bufferStrategy
        var failed = false
        try {
            bs.drawGraphics as Graphics2D
        } catch (e: NullPointerException) {
            failed = true
        }
        if (failed) {
            initializeBufferStrategy()
        }
    }
}

private fun Tile.finalTileset(graphics: TileGraphics): TilesetResource {
    return if (this is TilesetHolder) {
        tileset
    } else graphics.tileset
}