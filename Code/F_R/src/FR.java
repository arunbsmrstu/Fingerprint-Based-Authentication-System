import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import javax.imageio.ImageIO;


class node
{
//  public ArrayList<Integer> neighbooringCellIndices=new ArrayList<Integer>();
    public int X,Y,Theta,Type;
    public node(int X,int Y,int Type)
    {
        this.X=X;
        this.Y=Y;
        this.Type=Type;
    }
}
public class FR
{

    /**
     * Greyscale & Binarizing 
     * @param img Image to be binarized based on the greyscaling at
     * @see http://manthapavankumar.wordpress.com/2012/12/16/converting-a-color-image-to-a-grayscale-image-programatically-using-java/
    */
    
    public static BufferedImage gabor_filter(BufferedImage img) throws Exception{
        
                int width = img.getWidth(null);
		int height = img.getHeight(null);
		sharpen_image shar = new sharpen_image();
                
                Histogram n =new Histogram();
        
                 int[][] greyScale = n.RGBToGrey(img);
                 n.makeNewBufferedImage(greyScale, width,height);

		int[][] array = new int[height+100][width+100];
		int[][] filter_arr = new int[height+100][width+100];
		int[][] norm_arr = new int[height+100][width+100];
		int[][] sharp_arr = new int[height+100][width+100];
		int[][] clear_arr = new int[height+100][width+100];
		int[][] foreground_arr = new int[height+100][width+100];
                
                estimate gf =new estimate();
		array = shar.img_arr(img);
		//long process = System.currentTimeMillis();
                
                clear_arr = shar.Clear(array,17,width,height);
                
                filter_arr = gf.orientation(clear_arr,width,height,17,7);
		//long endtime = System.currentTimeMillis();
		//System.out.println("runtime:filter   "+(endtime-process));
		BufferedImage filtered1 = shar.arr_img(filter_arr,width,height);	
		File outputfile1 = new File("Gabor_output.png");
		ImageIO.write(filtered1, "png", outputfile1);
        
        
        
        return filtered1;
    
 }
    
    
    
    
    
    
    
    public static int[][] binarizing(BufferedImage img) throws Exception
    {
        int i,j,avg=0;
        int img_bin[][]=new int[img.getWidth()][img.getHeight()];
        BufferedImage greyscale=new BufferedImage(img.getWidth(),img.getHeight(),img.getType());
        Color c;
        for(i=0;i<img.getWidth();i++)
            for(j=0;j<img.getHeight();j++)
            {
                c=new Color(img.getRGB(i, j));
                
                avg=c.getRed()+c.getGreen()+c.getBlue();
                avg/=3;
                if(avg>127)
                    img_bin[i][j]=1;
                else
                    img_bin[i][j]=0;
                
                greyscale.setRGB(i, j, new Color(avg,avg,avg).getRGB());
            }
        ImageIO.write(greyscale, "png", new File("binarizing.png"));
        return img_bin;
    }
    public static int[][] thining(int img_bin[][]) throws Exception
    {
        int i,j,k,A,B;
        //clockwise
        int di[]=new int[]{0,0, -1,-1,0,1,1,1,0,-1};//1st 2 elements are useless
        int dj[]=new int[]{0,0, 0,1,1,1,0,-1,-1,-1};
        boolean EVEN=true;
        
        BufferedImage debugImg=new BufferedImage(img_bin.length, img_bin[0].length, BufferedImage.TYPE_INT_ARGB);
        
        int skeleton[][]=new int[img_bin.length][img_bin[0].length];
        for(i=1;i<img_bin.length-1;i++)
            for(j=1;j<img_bin[i].length-1;j++)
            {
                skeleton[i][j]=0;
                A=img_bin[i][j];
                B=0;
                for(k=2;k<=9;k++)// from P2+P3+...+P9
                    B+=img_bin[i+di[k]][j+dj[k]];
                
                // A=1  AND 3<= B <=6?
                if(A==1 && 3<=B && B<=6)
                {
                    if(     !EVEN
                            &&
                            img_bin[i+di[2]][j+dj[2]]*
                            img_bin[i+di[4]][j+dj[4]]*
                            img_bin[i+di[6]][j+dj[6]] == 0
                            &&
                            img_bin[i+di[4]][j+dj[4]]*
                            img_bin[i+di[6]][j+dj[6]]*
                            img_bin[i+di[8]][j+dj[8]] == 0)
                        
                                skeleton[i][j]=1;
                    
                    
                    if(     EVEN
                            &&
                            img_bin[i+di[2]][j+dj[2]]*
                            img_bin[i+di[4]][j+dj[4]]*
                            img_bin[i+di[8]][j+dj[8]] == 0
                            &&
                            img_bin[i+di[2]][j+dj[2]]*
                            img_bin[i+di[6]][j+dj[6]]*
                            img_bin[i+di[8]][j+dj[8]] == 0)
                        
                                skeleton[i][j]=1;
                    
                        
                }
                
                if(skeleton[i][j]==0)
                        debugImg.setRGB(i, j, new Color(255, 255, 255).getRGB());
                else
                        debugImg.setRGB(i, j, new Color(0, 0, 0).getRGB());
                
                EVEN=!EVEN;
            }
        
        ImageIO.write(debugImg, "png", new File("thin.png"));
        return skeleton;
    }
    
    public static int[][] getCN(int skeleton[][])//after thinning
    {
        int di[]=new int[]{0,-1,-1,-1,0,1,1,1};
        int dj[]=new int[]{1,1,0,-1,-1,-1,0,1};
        
        int i,j,k;
        
        
        int trimL=skeleton[0].length-1;
       // System.out.println(trimL+"\n");
        int trimT=skeleton.length-1;
        // System.out.println(trimT+"\n");
        for(i=0;i<skeleton.length;i++)
            for(j=0;j<skeleton[0].length;j++)
                if(skeleton[i][j]>0)
                {
                    trimT=Math.min(trimT, i);
                  // System.out.println(trimT+"\n");
                    trimL=Math.min(trimL, j);
                   // System.out.println(trimL+"\n");
                }
                
        int CN[][]=new int[skeleton.length-trimT][skeleton[0].length-trimL];
       
       // System.out.println(trimL);
      // System.out.println(trimT);
       
    
    
        
        for(i=trimT;i<skeleton.length-1;i++)
            for(j=trimL;j<skeleton[0].length-1;j++)
            {
                CN[i-trimT][j-trimL]=0;
                for(k=1;k<8;k++){
                    CN[i-trimT][j-trimL]+=Math.abs(skeleton[i+di[k-1]][j+dj[k-1]]-skeleton[i+di[k]][j+dj[k]]);
              // System.out.println(CN[i-trimT][j-trimL]);
                
                if( CN[i-trimT][j-trimL]==1 || CN[i-trimT][j-trimL]==3){
                    CN[i-trimT][j-trimL]=1;
                } 
                
                
                
                }
                
                
                
            }
                   
            
       // System.out.println(CN);
       // System.out.println(Arrays.toString(CN));
        return CN;
    }
    
    public static node[] genGraph(int CN[][])
    {
        ArrayList<node> graph=new ArrayList<node>();
        int i,j,k,currentIndex=-1;
        int nod=0;
        for(i=0;i<CN.length;i++)
            for(j=0;j<CN[0].length;j++)
                if(CN[i][j]==1)
                {
//                    currentIndex= graph.size()-1;
//                    if(graph.size()==0)
//                        currentIndex=0;
                    
                    
                    // System.out.println(CN[i][j]);
                    graph.add(new node(i, j, CN[i][j]));
                    nod++;
                    //TODO: Heap out of memory fix
//                    for(k=0;k<CN.length || k<CN[i].length;k++) // K is the distance between center point & neighbooring cell(s)
//                    {
//                        if(i+k<CN.length
//                                && CN[i+k][j]>0)
//                        {
//                            graph.get(currentIndex).neighbooringCellIndices.add(graph.size());
//                            graph.add(new node(i+k, j, CN[i+k][j]));
//                        }
//                        if(j+k<CN[0].length
//                                && CN[i][j+k]>0)
//                        {
//                            graph.get(currentIndex).neighbooringCellIndices.add(graph.size());
//                            graph.add(new node(i, j+k, CN[i][j+k]));
//                        }
//                        
//                    }
                    
                    
                }
        
        
        node    GraphNodes[]=new node[graph.size()];
        graph.toArray(GraphNodes);
        
       //System.out.println("TOtal node"+nod);
        
        return GraphNodes;
    }
    
    public static int compare(node[] graph1,node[] graph2)
    {
        
        int MinimumDistance=Integer.MAX_VALUE,missing=0;
        
        Queue<Integer> Q=new LinkedList<Integer>();
        int i,j,currentBest,currentCost,currentNode1=0,currentNode2=0,TotalDifference=0;
        
        boolean used[]=new boolean[graph2.length];
        Arrays.fill(used, false);
        
        int hit=0,nods=0,matched_node=0;
        currentNode1=0;
        TotalDifference=0;
        
        Q.add(currentNode1);
        Q.add(TotalDifference);
        
        //System.out.println("Graph length:"+graph1.length);
        //System.out.println("Graph length2:"+graph2.length);
        
        while(!Q.isEmpty())
        {
            currentNode1=Q.remove();
           // nods++;
            TotalDifference=Q.remove();
            
            currentCost=0;
            //System.out.println("currentCost:"+currentCost);
            
            currentBest=-1;
            
            if(currentNode1==graph1.length-1)
            {
                MinimumDistance=Math.min(MinimumDistance,TotalDifference);
                //System.out.println("Arun");
                break;
                
            }
            
            
            for(i=0;i<graph2.length;i++)
                if(graph1[currentNode1].Type== graph2[i].Type && used[i]==false &&
                        
                    (Math.abs(graph1[currentNode1].X-graph2[i].X)+
                     Math.abs(graph1[currentNode1].Y-graph2[i].Y))<=TotalDifference
                  )
                {
                    
                    
                  //TODO: branching should be here, but returns Heap out of memory,so, is picking the closest 1 w same type fine?!
                    
                  currentCost=(Math.abs(graph1[currentNode1].X-graph2[i].X)+
                                Math.abs(graph1[currentNode1].Y-graph2[i].Y));
                   hit++;
                  currentBest=i;  
                  
                  
                }
            
          //  System.out.println("Currentbest:"+currentBest);
            if(currentBest>0)
            {
                used[currentBest]=true;
                matched_node++;
                Q.add(currentNode1+1);
                Q.add(TotalDifference+currentCost);
            }
            else
            {
                missing++;
                Q.add(currentNode1+1);
                Q.add(TotalDifference);
            }
            
        }
        System.out.println("Matched node="+matched_node);
        System.out.println("Skipped="+missing);
       // System.out.println("Nods="+nods);
        System.out.println("Hits="+hit);
        
        float a=(float) matched_node/(float) graph1.length;
        //int b= (int)a/100;
        //System.out.println("Score:"+hit/graph1.length); 
        //new DecimalFormat("##.##").format(i2)
         System.out.println("Accuracy:"+a);
         System.out.println("Total NOde:"+graph2.length);
        
        return MinimumDistance;
    }
    
    
    public static void main(String[] args) throws Exception
    {
        int i;
        node[] graph1=
             genGraph( 
                    getCN(
                        thining(
                            binarizing(
                                
                                     gabor_filter(  ImageIO.read(new File("C:\\Users\\AB47\\Documents\\NetBeansProjects\\F_R\\samples/1.png")))))));
       
                                                                        
        for(i=1;i<=10;i++)      
            System.out.println("Distance"+i+":"+
                compare(graph1, 
                    genGraph( 
                        getCN(
                           thining(
                                binarizing(
                                        
                                 gabor_filter(ImageIO.read(new File("C:\\Users\\AB47\\Documents\\NetBeansProjects\\F_R\\samples/"+i+".png")) 
                                
                                )))) )
                ));
      

    }

}