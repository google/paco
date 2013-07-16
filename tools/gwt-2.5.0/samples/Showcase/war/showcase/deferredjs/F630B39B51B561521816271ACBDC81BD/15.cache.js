function ly(){}
function sy(){}
function Ly(){}
function Rj(a,b){Ej(b,a)}
function ky(a,b){UQb(b.b,a)}
function ry(a,b){VQb(b.b,a)}
function Ky(a,b){WQb(b.b,a)}
function Cqb(a){this.b=a}
function Jqb(a){this.b=a}
function mRb(a){this.b=a}
function GRb(a){this.b=a}
function aRb(a){a.g=false;YJb(a.db)}
function WQb(a,b){aRb(a,(a.b,qx(b),rx(b)))}
function UQb(a,b){$Qb(a,(a.b,qx(b)),rx(b))}
function VQb(a,b){_Qb(a,(a.b,qx(b)),rx(b))}
function cRb(){dk();dRb.call(this,new ERb)}
function Jy(){Jy=anc;Iy=new Ex(ttc,new Ly)}
function qy(){qy=anc;py=new Ex(qtc,new sy)}
function jy(){jy=anc;iy=new Ex(Vsc,new ly)}
function bRb(a){!a.i&&(a.i=HKb(new mRb(a)));uk(a)}
function XQb(a){if(a.i){wbc(a.i.b);a.i=null}ik(a,false)}
function $Qb(a,b,c){if(!SJb){a.g=true;ZJb(a.db);a.e=b;a.f=c}}
function ERb(){tRb();BRb.call(this);this.db[cqc]='Caption'}
function NQb(a,b){var c,d;d=lLb(a.c,b);c=lLb(d,1);return Fr(c)}
function oNb(a,b,c){var d;d=nNb(a,b);!!d&&(d[Vtc]=c.b,undefined)}
function ZQb(a,b){A4b(a.db,Dpc,b);Ui(a.b,b+'-caption');A4b(NQb(a.k,1),b,wwc)}
function YQb(a,b){var c;c=b.target;if(Cr(c)){return Zr(Hr(NQb(a.k,0)),c)}return false}
function _Qb(a,b,c){var d,e;if(a.g){d=b+rr(a.db);e=c+sr(a.db);if(d<a.c||d>=a.j||e<a.d){return}pk(a,d-a.e,e-a.f)}}
function qx(a){var b,c;b=a.c;if(b){return c=a.b,(c.clientX||0)-Rr(b)+Xr(b)+js(b.ownerDocument)}return a.b.clientX||0}
function rx(a){var b,c;b=a.c;if(b){return c=a.b,(c.clientY||0)-Tr(b)+(b.scrollTop||0)+ks(b.ownerDocument)}return a.b.clientY||0}
function ok(a){a.x=true;if(!a.t){a.t=$doc.createElement(hqc);a.t.className='gwt-PopupPanelGlass';a.t.style[vrc]=(Ou(),wrc);a.t.style[yrc]=0+(Lv(),Mqc);a.t.style[zrc]=Arc}}
function dRb(a){var b,c;iQb.call(this,false,true,Zoc);Cj(a);this.b=a;c=NQb(this.k,0);TJb(c,this.b.db);Rj(this,this.b);T5b(Fr(this.db))[cqc]='gwt-DialogBox';this.j=gs($doc);this.c=Vr($doc);this.d=Wr($doc);b=new GRb(this);vj(this,b,(jy(),jy(),iy));vj(this,b,(Jy(),Jy(),Iy));vj(this,b,(qy(),qy(),py));vj(this,b,(Dy(),Dy(),Cy));vj(this,b,(xy(),xy(),wy))}
function yqb(){var a,b,c,d,e,f,g,i,j,k,n;a=(g=new cRb,ZQb(g,'cwDialogBox'),uRb(g.b,'Sample DialogBox'),i=new Y4b,i.f[Xtc]=4,Wj(g.k,i),jk(g),j=new DRb('This is an example of a standard dialog box component.'),V4b(i,j),oNb(i,j,(jVb(),dVb)),k=new aJb((w6(),l6)),V4b(i,k),oNb(i,k,dVb),n=new lNb(mwc,new Jqb(g)),V4b(i,n),eE(),oNb(i,n,iVb),g);ok(a);a.w=true;e=new lNb('Show Dialog Box',new Cqb(a));d=new DRb('<br><br><br>This list box demonstrates that you can drag the popup over it. This obscure corner case renders incorrectly for many other libraries.');c=new TWb;c.db.size=1;for(b=10;b>0;--b){PWb(c,Kwc+b,Kwc+b,-1)}f=new Y4b;f.f[Xtc]=8;V4b(f,e);V4b(f,d);V4b(f,c);return f}
var Kwc='item ';L1(290,278,{},ly);_.Bc=function my(a){ky(this,YH(a,38))};_.Ec=function ny(){return iy};var iy;L1(291,278,{},sy);_.Bc=function ty(a){ry(this,YH(a,39))};_.Ec=function uy(){return py};var py;L1(294,278,{},Ly);_.Bc=function My(a){Ky(this,YH(a,42))};_.Ec=function Ny(){return Iy};var Iy;L1(736,1,Unc,Cqb);_.Hc=function Dqb(a){fk(this.b);bRb(this.b)};_.b=null;L1(737,1,Xnc);_.qc=function Hqb(){o4(this.b,yqb())};L1(738,1,Unc,Jqb);_.Hc=function Kqb(a){XQb(this.b)};_.b=null;L1(1002,998,mnc,cRb);_.Qb=function eRb(){try{zj(this.k)}finally{zj(this.b)}};_.Rb=function fRb(){try{Bj(this.k)}finally{Bj(this.b)}};_.ec=function gRb(){XQb(this)};_.Vb=function hRb(a){switch(_Kb(a.type)){case 4:case 8:case 64:case 16:case 32:if(!this.g&&!YQb(this,a)){return}}Aj(this,a)};_.Ib=function iRb(a){ZQb(this,a)};_.fc=function jRb(a){var b;b=a.e;!a.b&&_Kb(a.e.type)==4&&YQb(this,b)&&(b.preventDefault(),undefined);a.d&&(a.e,false)&&(a.b=true)};_.gc=function kRb(){bRb(this)};_.b=null;_.c=0;_.d=0;_.e=0;_.f=0;_.g=false;_.i=null;_.j=0;L1(1003,1,doc,mRb);_.Oc=function nRb(a){this.b.j=a.b};_.b=null;L1(1004,1005,knc,ERb);L1(1008,1,{38:1,39:1,40:1,41:1,42:1,54:1},GRb);_.Kc=function HRb(a){};_.Lc=function IRb(a){};_.b=null;var US=kcc(Puc,'CwDialogBox$1',736),WS=kcc(Puc,'CwDialogBox$3',738),FW=kcc(Luc,'DialogBox',1002),DW=kcc(Luc,'DialogBox$CaptionImpl',1004),EW=kcc(Luc,'DialogBox$MouseHandler',1008),CW=kcc(Luc,'DialogBox$1',1003),yM=kcc(jvc,'MouseDownEvent',290),DM=kcc(jvc,'MouseUpEvent',294),AM=kcc(jvc,'MouseMoveEvent',291);Koc(In)(15);