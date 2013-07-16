function Mx(){}
function Tx(){}
function ky(){}
function Gj(a,b){tj(b,a)}
function Lx(a,b){jQb(b.b,a)}
function Sx(a,b){kQb(b.b,a)}
function jy(a,b){lQb(b.b,a)}
function $pb(a){this.b=a}
function fqb(a){this.b=a}
function DQb(a){this.b=a}
function XQb(a){this.b=a}
function rQb(a){a.g=false;rJb(a.db)}
function tQb(){uQb.call(this,new VQb)}
function lQb(a,b){rQb(a,(a.b,Rw(b),Sw(b)))}
function jQb(a,b){pQb(a,(a.b,Rw(b)),Sw(b))}
function kQb(a,b){qQb(a,(a.b,Rw(b)),Sw(b))}
function sQb(a){!a.i&&(a.i=bKb(new DQb(a)));ik(a)}
function mQb(a){if(a.i){Gac(a.i.b);a.i=null}Yj(a,false)}
function iy(){iy=kmc;hy=new dx(Bsc,new ky)}
function Kx(){Kx=kmc;Jx=new dx(dsc,new Mx)}
function Rx(){Rx=kmc;Qx=new dx(ysc,new Tx)}
function VQb(){KQb();SQb.call(this);this.db[lpc]='Caption'}
function pQb(a,b,c){if(!lJb){a.g=true;sJb(a.db);a.e=b;a.f=c}}
function cQb(a,b){var c,d;d=HKb(a.c,b);c=HKb(d,1);return rr(c)}
function FMb(a,b,c){var d;d=EMb(a,b);!!d&&(d[Zsc]=c.b,undefined)}
function oQb(a,b){V3b(a.db,Moc,b);Ji(a.b,b+'-caption');V3b(cQb(a.k,1),b,zvc)}
function nQb(a,b){var c;c=b.target;if(or(c)){return Cr(tr(cQb(a.k,0)),c)}return false}
function qQb(a,b,c){var d,e;if(a.g){d=b+Er(a.db);e=c+Fr(a.db);if(d<a.c||d>=a.j||e<a.d){return}dk(a,d-a.e,e-a.f)}}
function Sw(a){var b,c;b=a.c;if(b){return c=a.b,(c.clientY||0)-Fr(b)+(b.scrollTop||0)+Mr(b.ownerDocument)}return a.b.clientY||0}
function Rw(a){var b,c;b=a.c;if(b){return c=a.b,(c.clientX||0)-Er(b)+(b.scrollLeft||0)+Lr(b.ownerDocument)}return a.b.clientX||0}
function ck(a){a.x=true;if(!a.t){a.t=$doc.createElement(qpc);a.t.className='gwt-PopupPanelGlass';a.t.style[Fqc]=(nu(),Gqc);a.t.style[Iqc]=0+(kv(),Vpc);a.t.style[Jqc]=Kqc}}
function uQb(a){var b,c;zPb.call(this,false,true,goc);rj(a);this.b=a;c=cQb(this.k,0);mJb(c,this.b.db);Gj(this,this.b);tr(rr(this.db))[lpc]='gwt-DialogBox';this.j=Ir($doc);this.c=0;this.d=0;b=new XQb(this);kj(this,b,(Kx(),Kx(),Jx));kj(this,b,(iy(),iy(),hy));kj(this,b,(Rx(),Rx(),Qx));kj(this,b,(cy(),cy(),by));kj(this,b,(Yx(),Yx(),Xx))}
function Wpb(){var a,b,c,d,e,f,g,i,j,k,n;a=(g=new tQb,oQb(g,'cwDialogBox'),LQb(g.b,'Sample DialogBox'),i=new r4b,i.f[_sc]=4,Lj(g.k,i),Zj(g),j=new UQb('This is an example of a standard dialog box component.'),o4b(i,j),FMb(i,j,(EUb(),yUb)),k=new vIb((U5(),J5)),o4b(i,k),FMb(i,k,yUb),n=new CMb(pvc,new fqb(g)),o4b(i,n),FD(),FMb(i,n,DUb),g);ck(a);a.w=true;e=new CMb('Show Dialog Box',new $pb(a));d=new UQb('<br><br><br>This list box demonstrates that you can drag the popup over it. This obscure corner case renders incorrectly for many other libraries.');c=new mWb;c.db.size=1;for(b=10;b>0;--b){iWb(c,Nvc+b,Nvc+b,-1)}f=new r4b;f.f[_sc]=8;o4b(f,e);o4b(f,d);o4b(f,c);return f}
var Nvc='item ';h1(289,277,{},Mx);_.xc=function Nx(a){Lx(this,xH(a,38))};_.Ac=function Ox(){return Jx};var Jx;h1(290,277,{},Tx);_.xc=function Ux(a){Sx(this,xH(a,39))};_.Ac=function Vx(){return Qx};var Qx;h1(293,277,{},ky);_.xc=function ly(a){jy(this,xH(a,42))};_.Ac=function my(){return hy};var hy;h1(735,1,bnc,$pb);_.Dc=function _pb(a){Vj(this.b);sQb(this.b)};_.b=null;h1(736,1,enc);_.mc=function dqb(){M3(this.b,Wpb())};h1(737,1,bnc,fqb);_.Dc=function gqb(a){mQb(this.b)};_.b=null;h1(998,994,vmc,tQb);_.Mb=function vQb(){try{oj(this.k)}finally{oj(this.b)}};_.Nb=function wQb(){try{qj(this.k)}finally{qj(this.b)}};_.ac=function xQb(){mQb(this)};_.Rb=function yQb(a){switch(vKb(a.type)){case 4:case 8:case 64:case 16:case 32:if(!this.g&&!nQb(this,a)){return}}pj(this,a)};_.Eb=function zQb(a){oQb(this,a)};_.bc=function AQb(a){var b;b=a.e;!a.b&&vKb(a.e.type)==4&&nQb(this,b)&&(b.preventDefault(),undefined);a.d&&(a.e,false)&&(a.b=true)};_.cc=function BQb(){sQb(this)};_.b=null;_.c=0;_.d=0;_.e=0;_.f=0;_.g=false;_.i=null;_.j=0;h1(999,1,mnc,DQb);_.Kc=function EQb(a){this.b.j=a.b};_.b=null;h1(1000,1001,tmc,VQb);h1(1004,1,{38:1,39:1,40:1,41:1,42:1,54:1},XQb);_.Gc=function YQb(a){};_.Hc=function ZQb(a){};_.b=null;var rS=ubc(Stc,'CwDialogBox$1',735),tS=ubc(Stc,'CwDialogBox$3',737),aW=ubc(Otc,'DialogBox',998),$V=ubc(Otc,'DialogBox$CaptionImpl',1000),_V=ubc(Otc,'DialogBox$MouseHandler',1004),ZV=ubc(Otc,'DialogBox$1',999),WL=ubc(muc,'MouseDownEvent',289),_L=ubc(muc,'MouseUpEvent',293),YL=ubc(muc,'MouseMoveEvent',290);Tnc(wn)(15);