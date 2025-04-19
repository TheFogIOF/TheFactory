package tfmg.main.research;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.input.KeyCode;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.scene.Action;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.actions.Actions;
import arc.scene.actions.RelativeTemporalAction;
import arc.scene.event.ElementGestureListener;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.ui.ImageButton;
import arc.scene.ui.Label;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.Scaling;
import arc.util.Structs;
import java.util.Arrays;
import java.util.Objects;
import mindustry.Vars;
import mindustry.content.Planets;
import mindustry.content.TechTree;
import mindustry.core.UI;
import mindustry.game.EventType;
import mindustry.game.Objectives;
import mindustry.gen.Icon;
import mindustry.gen.Sounds;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.input.Binding;
import mindustry.type.Item;
import mindustry.type.ItemSeq;
import mindustry.type.ItemStack;
import mindustry.type.Planet;
import mindustry.type.Sector;
import mindustry.ui.Fonts;
import mindustry.ui.ItemsDisplay;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.PlanetDialog;
import mindustry.ui.layout.BranchTreeLayout;
import mindustry.ui.layout.TreeLayout;
import tfmg.main.items.ResearchPlaceholderItem;
import tfmg.main.registry.Items;
import tfmg.main.registry.Textures;

import static mindustry.Vars.iconMed;
import static mindustry.content.TechTree.*;

public class CustomResearchDialog extends BaseDialog {
    public static boolean debugShowRequirements = false;
    public final float nodeSize = Scl.scl(60.0F);
    public ObjectSet<TechTreeNode> nodes = new ObjectSet();
    public TechTreeNode root;
    public TechTree.TechNode lastNode;
    public Rect bounds;
    private boolean needsRebuild;
    public ItemsDisplay itemDisplay;
    public View view;
    public ItemSeq items;
    private boolean showTechSelect;


    public CustomResearchDialog(){
        super("");
        root = new TechTreeNode((TechTree.TechNode)TechTree.roots.first(), (TechTreeNode)null);
        lastNode = this.root.node;
        bounds = new Rect();

        Events.on(EventType.ResetEvent.class, e -> {
            hide();
        });

        Events.on(EventType.UnlockEvent.class, e -> {
            if(Vars.net.client() && !needsRebuild){
                needsRebuild = true;
                Core.app.post(() -> {
                    needsRebuild = false;

                    checkNodes(root);
                    view.hoverNode = null;
                    treeLayout();
                    view.rebuild();
                    Core.scene.act();
                });
            }
        });

        titleTable.remove();
        titleTable.clear();
        titleTable.top();
        titleTable.button(b -> {
            //TODO custom icon here.
            b.imageDraw(() -> root.node.icon()).padRight(8).size(iconMed);
            b.add().growX();
            b.label(() -> root.node.localizedName()).color(Pal.accent);
            b.add().growX();
            b.add().size(iconMed);
        }, () -> {
            new BaseDialog("@techtree.select"){{
                cont.pane(t -> {
                    t.table(Tex.button, in -> {
                        in.defaults().width(300f).height(60f);
                        for(TechNode node : TechTree.roots){
                            if(node.requiresUnlock && !node.content.unlockedHost() && node != getPrefRoot()) continue;

                            //TODO toggle
                            in.button(node.localizedName(), node.icon(), Styles.flatTogglet, iconMed, () -> {
                                if(node == lastNode){
                                    return;
                                }

                                rebuildTree(node);
                                hide();
                            }).marginLeft(12f).checked(node == lastNode).row();
                        }
                    });
                });

                addCloseButton();
            }}.show();
        }).visible(() -> showTechSelect = TechTree.roots.count(node -> !(node.requiresUnlock && !node.content.unlockedHost())) > 1).minWidth(300f);

        margin(0.0F).marginBottom(8.0F);
        cont.stack(new Element[]{titleTable, view = new View(), itemDisplay = new ItemsDisplay()}).grow();
        itemDisplay.visible(() -> !Vars.net.client());
        titleTable.toFront();
        shouldPause = true;
        onResize(this::checkMargin);
        shown(() -> {
            checkMargin();
            Core.app.post(this::checkMargin);
            Planet currPlanet = Vars.ui.planet.isShown() ? Vars.ui.planet.state.planet : (Vars.state.isCampaign() ? Vars.state.rules.sector.planet : null);
            if (currPlanet != null && currPlanet.techTree != null) {
                this.switchTree(currPlanet.techTree);
            }

            rebuildItems();
            checkNodes(root);
            treeLayout();
            view.hoverNode = null;
            view.infoTable.remove();
            view.infoTable.clear();
        });
        PlanetDialog var10001 = Vars.ui.planet;
        Objects.requireNonNull(var10001);
        //this.hidden(var10001::setup);

        addCloseButton();
        keyDown((key) -> {
            if (key == Core.keybinds.get(Binding.research).key) {
                Core.app.post(this::hide);
            }

        });
        buttons.button("@database", Icon.book, () -> {
            this.hide();
            Vars.ui.database.show();
        }).size(210.0F, 64.0F).name("database");
        addListener(new InputListener() {
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
                view.setScale(Mathf.clamp(view.scaleX - amountY / 10.0F * view.scaleX, 0.25F, 1.0F));
                view.setOrigin(1);
                view.setTransform(true);
                return true;
            }

            public boolean mouseMoved(InputEvent event, float x, float y) {
                view.requestScroll();
                return super.mouseMoved(event, x, y);
            }
        });
        touchable = Touchable.enabled;
        addCaptureListener(new ElementGestureListener() {
            public void zoom(InputEvent event, float initialDistance, float distance) {
                if (view.lastZoom < 0.0F) {
                    view.lastZoom = view.scaleX;
                }

                view.setScale(Mathf.clamp(distance / initialDistance * view.lastZoom, 0.25F, 1.0F));
                view.setOrigin(1);
                view.setTransform(true);
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button) {
                view.lastZoom = view.scaleX;
            }

            public void pan(InputEvent event, float x, float y, float deltaX, float deltaY) {
                View var10000 = view;
                var10000.panX += deltaX / view.scaleX;
                var10000 = view;
                var10000.panY += deltaY / view.scaleY;
                view.moved = true;
                view.clamp();
            }
        });
    }

    void checkMargin() {
        if (Core.graphics.isPortrait() && this.showTechSelect) {
            itemDisplay.marginTop(60.0F);
        } else {
            itemDisplay.marginTop(0.0F);
        }

        itemDisplay.invalidate();
        itemDisplay.layout();
    }

    public void rebuildItems() {
        items = new ItemSeq() {
            ObjectMap<Sector, ItemSeq> cache = new ObjectMap();

            {
                Planet rootPlanet = lastNode.planet != null ? lastNode.planet : (Planet)Vars.content.planets().find((p) -> {
                    return p.techTree == lastNode;
                });
                if (rootPlanet == null) rootPlanet = Planets.serpulo;

                for(Sector sector : rootPlanet.sectors){
                    if(sector.hasBase()){
                        ItemSeq cached = sector.items();
                        cache.put(sector, cached);
                        cached.each((item, amount) -> {
                            values[item.id] += Math.max(amount, 0);
                            total += Math.max(amount, 0);
                        });
                    }
                }
            }

            @Override
            public void add(Item item, int amount) {
                if (amount < 0) {
                    amount = -amount;
                    double percentage = (double) amount / (double) get(item);
                    int[] counter = new int[]{amount};
                    cache.each((sector, seq) -> {
                        if(counter[0] == 0) return;
                        int toRemove = Math.min((int)Math.ceil(percentage * seq.get(item)), counter[0]);
                        sector.removeItem(item, toRemove);
                        seq.remove(item, toRemove);
                        counter[0] -= toRemove;
                    });
                    amount = -amount;
                }
                super.add(item, amount);
            }
        };
        itemDisplay.rebuild(items);
    }

    @Nullable
    public TechTree.TechNode getPrefRoot() {
        Planet currPlanet = Vars.ui.planet.isShown() ? Vars.ui.planet.state.planet : (Vars.state.isCampaign() ? Vars.state.rules.sector.planet : null);
        return currPlanet == null ? null : currPlanet.techTree;
    }

    public void switchTree(TechTree.TechNode node) {
        if (lastNode != node && node != null) {
            nodes.clear();
            root = new TechTreeNode(node, null);
            lastNode = node;
            view.rebuildAll();
            rebuildItems();
        }
    }

    public void rebuildTree(TechTree.TechNode node) {
        this.switchTree(node);
        view.panX = 0.0F;
        view.panY = -200.0F;
        view.setScale(1.0F);
        view.hoverNode = null;
        view.infoTable.remove();
        view.infoTable.clear();
        this.checkNodes(this.root);
        this.treeLayout();
    }

    void treeLayout() {
        final float spacing = 20.0F;
        LayoutNode node = new LayoutNode(this.root, (LayoutNode)null);
        LayoutNode[] children = node.children;
        LayoutNode[] leftHalf = Arrays.copyOfRange(node.children, 0, Mathf.ceil((float) node.children.length / 2.0F));
        LayoutNode[] rightHalf = Arrays.copyOfRange(node.children, Mathf.ceil((float) node.children.length / 2.0F), node.children.length);
        node.children = leftHalf;
        (new BranchTreeLayout() {
            {
                gapBetweenLevels = gapBetweenNodes = spacing;
                rootLocation = TreeLocation.top;
            }
        }).layout(node);
        float lastY = node.y;
        if (rightHalf.length > 0) {
            node.children = rightHalf;
            (new BranchTreeLayout() {
                {
                    gapBetweenLevels = gapBetweenNodes = spacing;
                    rootLocation = TreeLocation.bottom;
                }
            }).layout(node);
            this.shift(leftHalf, node.y - lastY);
        }

        node.children = children;
        float minx = 0.0F;
        float miny = 0.0F;
        float maxx = 0.0F;
        float maxy = 0.0F;
        this.copyInfo(node);

        for(TechTreeNode n : nodes){
            if(!n.visible) continue;
            minx = Math.min(n.x - n.width/2f, minx);
            maxx = Math.max(n.x + n.width/2f, maxx);
            miny = Math.min(n.y - n.height/2f, miny);
            maxy = Math.max(n.y + n.height/2f, maxy);
        }
        bounds = new Rect(minx, miny, maxx - minx, maxy - miny);
        bounds.y += nodeSize*1.5f;
    }

    void shift(LayoutNode[] children, float amount) {
        for(LayoutNode node : children){
            node.y += amount;
            if(node.children != null && node.children.length > 0) shift(node.children, amount);
        }
    }

    void copyInfo(LayoutNode node) {
        node.node.x = node.x;
        node.node.y = node.y;
        if(node.children != null){
            for(LayoutNode child : node.children){
                copyInfo(child);
            }
        }
    }

    void checkNodes(TechTreeNode node) {
        boolean locked = locked(node.node);
        if(!locked && (node.parent == null || node.parent.visible)) node.visible = true;
        node.selectable = selectable(node.node);
        for(TechTreeNode l : node.children){
            l.visible = !locked && l.parent.visible;
            checkNodes(l);
        }

        itemDisplay.rebuild(items);
    }

    boolean selectable(TechTree.TechNode node) {
        return node.content.unlockedHost() || !node.objectives.contains((i) -> {
            return !i.complete();
        });
    }

    boolean locked(TechTree.TechNode node) {
        return !node.content.unlockedHost();
    }

    public class TechTreeNode extends TreeLayout.TreeNode<TechTreeNode> {
        public final TechTree.TechNode node;
        public boolean visible = true;
        public boolean selectable = true;

        public TechTreeNode(TechTree.TechNode node, TechTreeNode parent) {
            this.node = node;
            this.parent = parent;
            this.width = this.height = nodeSize;
            nodes.add(this);
            children = new TechTreeNode[node.children.size];

            for(int i = 0; i < children.length; ++i) {
                children[i] = new TechTreeNode(node.children.get(i), this);
            }
        }
    }

    public class View extends Group {
        public float panX = 0, panY = -200, lastZoom = -1;
        public boolean moved = false;
        public ImageButton hoverNode;
        public Table infoTable = new Table();

        {
            rebuildAll();
        }

        public void rebuildAll() {
            clear();
            hoverNode = null;
            infoTable.clear();
            infoTable.touchable = Touchable.enabled;

            for(TechTreeNode node : nodes) {
                ImageButton button = new ImageButton(node.node.content.uiIcon, Styles.nodei);
                button.resizeImage(32.0F);
                button.getImage().setScaling(Scaling.fit);
                button.visible(() -> {
                    return true;
                });
                button.clicked(() -> {
                    if (!this.moved) {
                        if (Vars.mobile) {
                            hoverNode = button;
                            rebuild();
                            float right = infoTable.getRight();
                            if (right > (float) Core.graphics.getWidth()) {
                                final float moveBy = right - (float) Core.graphics.getWidth();
                                addAction(new RelativeTemporalAction() {
                                    {
                                        this.setDuration(0.1F);
                                        this.setInterpolation(Interp.fade);
                                    }

                                    protected void updateRelative(float percentDelta) {
                                        View var10000 = View.this;
                                        var10000.panX -= moveBy * percentDelta;
                                    }
                                });
                            }
                        } else if (canSpend(node.node) && locked(node.node) && node.visible) {
                            spend(node.node);
                        }
                    }
                });
                button.hovered(() -> {
                    if (!Vars.mobile && hoverNode != button) {
                        hoverNode = button;
                        rebuild();
                    }

                });
                button.exited(() -> {
                    if (!Vars.mobile && hoverNode == button && !infoTable.hasMouse() && !hoverNode.hasMouse()) {
                        hoverNode = null;
                        rebuild();
                    }

                });
                button.touchable(() -> {
                    return Touchable.enabled;
                });
                button.userObject = node.node;
                button.setSize(nodeSize);
                button.update(() -> {
                    float offset = (float)(Core.graphics.getHeight() % 2) / 2.0F;
                    button.setPosition(node.x + panX + width / 2.0F, node.y + panY + height / 2.0F + offset, 1);
                    button.getStyle().up = !locked(node.node) ? Tex.buttonOver : !selectable(node.node) || (!canSpend(node.node) && !Vars.net.client() || !node.visible) ? Tex.buttonRed : Tex.button;
                    if (isParentResearch(node.node) && !node.visible) button.getStyle().up = Textures.buttonParentResearch;
                    button.getImage().setColor(!locked(node.node) ? Color.white : (node.selectable ? Color.gray : Pal.gray));
                    button.getImage().layout();
                });
                this.addChild(button);
            }

            if (Vars.mobile) {
                this.tapped(() -> {
                    Element e = Core.scene.hit((float)Core.input.mouseX(), (float)Core.input.mouseY(), true);
                    if (e == this) {
                        this.hoverNode = null;
                        this.rebuild();
                    }

                });
            }

            this.setOrigin(1);
            this.setTransform(true);
            this.released(() -> {
                this.moved = false;
            });
        }

        void clamp() {
            float pad = nodeSize;
            float ox = this.width / 2.0F;
            float oy = this.height / 2.0F;
            float rx = bounds.x + this.panX + ox;
            float ry = this.panY + oy + bounds.y;
            float rw = bounds.width;
            float rh = bounds.height;
            rx = Mathf.clamp(rx, -rw + pad, (float)Core.graphics.getWidth() - pad);
            ry = Mathf.clamp(ry, -rh + pad, (float)Core.graphics.getHeight() - pad);
            this.panX = rx - bounds.x - ox;
            this.panY = ry - bounds.y - oy;
        }

        boolean canSpend(TechTree.TechNode node) {
            if (!selectable(node) || Vars.net.client()) return false;
            if (node.requirements.length == 0) return true;

            for(int i = 0; i < node.requirements.length; ++i) {
                if (node.finishedRequirements[i].amount < node.requirements[i].amount && items.has(node.requirements[i].item)) {
                    return true;
                }
            }

            return node.content.locked();
        }

        boolean isParentResearch(TechNode node) {
            return (node.requirements.length > 0 && node.requirements[0].item == Items.researchPlacehodler);
        }

        void spend(TechTree.TechNode node) {
            if(Vars.net.client()) return;

            boolean complete = true;
            boolean[] shine = new boolean[node.requirements.length];
            boolean[] usedShine = new boolean[Vars.content.items().size];

            for(int i = 0; i < node.requirements.length; ++i) {
                ItemStack req = node.requirements[i];
                ItemStack completed = node.finishedRequirements[i];
                int used = Math.max(Math.min(req.amount - completed.amount, items.get(req.item)), 0);
                items.remove(req.item, used);
                completed.amount += used;
                if (used > 0) {
                    shine[i] = true;
                    usedShine[req.item.id] = true;
                }

                if (completed.amount < req.amount) {
                    complete = false;
                }
            }

            if (complete) {
                this.unlock(node);
                for (TechNode unitedNode : node.children) if (isParentResearch(unitedNode)) this.unlock(unitedNode);
            }

            node.save();
            Core.scene.act();
            this.rebuild(shine);
            itemDisplay.rebuild(items, usedShine);
        }

        void unlock(TechTree.TechNode node) {
            node.content.unlock();

            TechNode parent = node.parent;
            while(parent != null){
                parent.content.unlock();
                parent = parent.parent;
            }

            checkNodes(root);
            hoverNode = null;
            treeLayout();
            rebuild();
            Core.scene.act();
            Sounds.unlock.play();
            Events.fire(new EventType.ResearchEvent(node.content));
        }

        void rebuild() {
            this.rebuild((boolean[])null);
        }

        void rebuild(@Nullable boolean[] shine) {
            ImageButton button = this.hoverNode;
            infoTable.remove();
            infoTable.clear();
            infoTable.update(null);
            if (button != null) {
                TechTree.TechNode node = (TechTree.TechNode)button.userObject;
                infoTable.exited(() -> {
                    if (hoverNode == button && !infoTable.hasMouse() && !hoverNode.hasMouse()) {
                        hoverNode = null;
                        rebuild();
                    }

                });
                infoTable.update(() -> {
                    this.infoTable.setPosition(button.x + button.getWidth(), button.y + button.getHeight(), 10);
                });
                infoTable.left();
                infoTable.background(Tex.button).margin(8.0F);
                boolean selectable = selectable(node);
                for (TechTreeNode techTreeNode : nodes)
                {
                    if (techTreeNode.node == node) {
                        infoTable.table((b) -> {
                            b.margin(0.0F).left().defaults().left();
                            if (selectable && techTreeNode.visible) {
                                b.button(Icon.info, Styles.flati, () -> {
                                    Vars.ui.content.show(node.content);
                                }).growY().width(50.0F);
                            }

                            b.add().grow();
                            b.table((desc) -> {
                                desc.left().defaults().left();
                                desc.add((selectable && techTreeNode.visible) ? node.content.localizedName : "[red]" + node.content.localizedName);
                                desc.row();
                                if (locked(node) || (debugShowRequirements && !Vars.net.client())) {
                                    if (Vars.net.client()) {
                                        desc.add("@locked").color(Pal.remove);
                                    } else {
                                        desc.table((t) -> {
                                            t.left();
                                            if (selectable && techTreeNode.visible) {
                                                if (Structs.contains(node.finishedRequirements, (s) -> {
                                                    return s.amount > 0;
                                                })) {
                                                    float sum = 0.0F;
                                                    float used = 0.0F;
                                                    boolean shinyx = false;

                                                    for(int i = 0; i < node.requirements.length; ++i) {
                                                        sum += node.requirements[i].item.cost * (float)node.requirements[i].amount;
                                                        used += node.finishedRequirements[i].item.cost * (float)node.finishedRequirements[i].amount;
                                                        if (shine != null) {
                                                            shinyx |= shine[i];
                                                        }
                                                    }

                                                    Label label = (Label)t.add(Core.bundle.format("research.progress", new Object[]{Math.min((int)(used / sum * 100.0F), 99)})).left().get();
                                                    if (shinyx) {
                                                        label.setColor(Pal.accent);
                                                        label.actions(new Action[]{Actions.color(Color.lightGray, 0.75F, Interp.fade)});
                                                    } else {
                                                        label.setColor(Color.lightGray);
                                                    }

                                                    t.row();
                                                }

                                                for(int ix = 0; ix < node.requirements.length; ++ix) {
                                                    ItemStack req = node.requirements[ix];
                                                    ItemStack completed = node.finishedRequirements[ix];
                                                    if (req.amount > completed.amount || debugShowRequirements) {
                                                        boolean shiny = shine != null && shine[ix];
                                                        t.table((list) -> {
                                                            int reqAmount = debugShowRequirements ? req.amount : req.amount - completed.amount;
                                                            list.left();
                                                            list.image(req.item.uiIcon).size(24.0F).padRight(3.0F);
                                                            list.add(req.item.localizedName).color(Color.lightGray);
                                                            Label label = (Label)list.label(() -> {
                                                                return " " + UI.formatAmount((long)Math.min(items.get(req.item), reqAmount)) + " / " + UI.formatAmount((long)reqAmount);
                                                            }).get();
                                                            Color targetColor = items.has(req.item) ? Color.lightGray : Color.scarlet;
                                                            if (shiny) {
                                                                label.setColor(Pal.accent);
                                                                label.actions(new Action[]{Actions.color(targetColor, 0.75F, Interp.fade)});
                                                            } else {
                                                                label.setColor(targetColor);
                                                            }

                                                        }).fillX().left();
                                                        t.row();
                                                    }
                                                }
                                            } else if (node.objectives.size > 0) {
                                                t.table((r) -> {
                                                    r.add("@complete").colspan(2).left();
                                                    r.row();
                                                    boolean hasParentResearch = true;
                                                    for (Objectives.Objective o : node.objectives) {
                                                        if (o.display().contains(node.parent.content.localizedName)) hasParentResearch = false;
                                                    }
                                                    if (hasParentResearch) {
                                                        r.add("> " + Core.bundle.format("requirement.research", new Object[]{node.parent.content.emoji() + " " + node.parent.content.localizedName + " "})).color(Color.lightGray);
                                                        r.row();
                                                    }
                                                    for (Objectives.Objective o : node.objectives) {
                                                        if (!o.complete()) {
                                                            r.add("> " + o.display()).color(Color.lightGray).left();
                                                            r.image(o.complete() ? Icon.ok : Icon.cancel, o.complete() ? Color.lightGray : Color.scarlet).padLeft(3.0F);
                                                            r.row();
                                                        }
                                                    }
                                                });
                                                t.row();
                                            }
                                        });
                                    }
                                } else {
                                    desc.add("@completed");
                                }
                            }).pad(9.0F);
                            if (Vars.mobile && locked(node) && techTreeNode.visible && !Vars.net.client()) {
                                b.row();
                                b.button("@research", Icon.ok, new TextButton.TextButtonStyle() {
                                    {
                                        this.disabled = Tex.button;
                                        this.font = Fonts.def;
                                        this.fontColor = Color.white;
                                        this.disabledFontColor = Color.gray;
                                        this.up = Tex.buttonOver;
                                        this.over = Tex.buttonDown;
                                    }
                                }, () -> {
                                    this.spend(node);
                                }).disabled((i) -> {
                                    return !this.canSpend(node);
                                }).growX().height(44.0F).colspan(3);
                            }

                        });
                        infoTable.row();
                        if (node.content.description != null && node.content.inlineDescription && selectable && techTreeNode.visible) {
                            infoTable.table((t) -> {
                                t.margin(3.0F).left().labelWrap(node.content.displayDescription()).color(Color.lightGray).growX();
                            }).fillX();
                        }
                    }
                }
                addChild(infoTable);
                checkMargin();
                Core.app.post(() -> {
                    checkMargin();
                });
                infoTable.pack();
                infoTable.act(Core.graphics.getDeltaTime());
            }
        }

        public void drawChildren() {
            this.clamp();
            float offsetX = this.panX + this.width / 2.0F;
            float offsetY = this.panY + this.height / 2.0F;
            Draw.sort(true);
            ObjectSet.ObjectSetIterator var3 = nodes.iterator();

            while(true) {
                boolean isfalse = false;
                TechTreeNode node;
                do {
                    if (!var3.hasNext()) {
                        Draw.sort(false);
                        Draw.reset();
                        super.drawChildren();
                        return;
                    }

                    node = (TechTreeNode)var3.next();
                } while(isfalse);

                TechTreeNode[] var5 = node.children;
                int var6 = var5.length;

                for(int var7 = 0; var7 < var6; ++var7) {
                    TechTreeNode child = var5[var7];
                    if (!isfalse) {
                        boolean lock = locked(node.node) || locked(child.node);
                        Draw.z(lock ? 1.0F : 2.0F);
                        Lines.stroke(Scl.scl(4.0F), lock ? Pal.gray : Pal.accent);
                        Draw.alpha(this.parentAlpha);
                        if (Mathf.equal(Math.abs(node.y - child.y), Math.abs(node.x - child.x), 1.0F) && Mathf.dstm(node.x, node.y, child.x, child.y) <= node.width * 3.0F) {
                            Lines.line(node.x + offsetX, node.y + offsetY, child.x + offsetX, child.y + offsetY);
                        } else {
                            Lines.line(node.x + offsetX, node.y + offsetY, child.x + offsetX, node.y + offsetY);
                            Lines.line(child.x + offsetX, node.y + offsetY, child.x + offsetX, child.y + offsetY);
                        }
                    }
                }
            }
        }
    }

    class LayoutNode extends TreeLayout.TreeNode<LayoutNode> {
        final TechTreeNode node;

        LayoutNode(TechTreeNode node, LayoutNode parent) {
            this.node = node;
            this.parent = parent;
            this.width = this.height = nodeSize;
            if (node.children != null) {
                this.children = Seq.with(node.children).map((t) -> {
                    return new LayoutNode(t, this);
                }).toArray(LayoutNode.class);
            }

        }
    }
}