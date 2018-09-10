//Dealing with iseTol=0/st/20 => only segments/mix segments and arcs/only arcs
vector<int> arcSegmentDecomposition(const vector<RealPoint> &aContour, const vector<int> &indexDP, const vector<RealPoint> &MP, double alphaMax, double thickness, double iseTol, double angleTol, int nbPointCir, vector<double> &segments, vector<double> &arcs)
{
	//[Author:Jochen] flag list: is something an arc or a segment. Indexes on midpoints
    vector<bool> isArc;
    for(size_t it=0; it<MP.size(); it++)
        isArc.push_back(false);

	//[Author:Jochen] blurredSegmentTS: TS as in Tomato and Salad?
    vector<AlphaThickSegmentComputer2DD> blurredSegmentTS;
	//[Author:Jochen] flag list, size is the same as midpoint list (MP). 
    vector<int> isolatedVector;//SEG:1,ARC:0,JOINCTION:-1
	//[Author:Jochen] totally unclear: how could something be an index list if it contains RealPoints
    vector<RealPoint> arcIndex;
    if(MP.size()<2)
        iseTol=0;
    if(iseTol==0.0)//min => only segments => nothing to do
        for(size_t it=0; it<MP.size(); it++)
            isolatedVector.push_back(1);
    else if(iseTol==20.0)//max => only arcs
    {
        for(size_t it=0; it<MP.size(); it++)
            isolatedVector.push_back(0);

        /*********** Decomposition into blurred segments ****/
        vector<RealPoint>::const_iterator it_MP=MP.begin();
        for(size_t it_start=0; it_start<MP.size(); it_start++)
        {
            int it_end=it_start;
            AlphaThickSegmentComputer2DD aSegment(thickness);
            aSegment.init(it_MP);
            while(aSegment.end()!=MP.end() && aSegment.extendFront())
                it_end++;
            it_MP++;
            if(blurredSegmentTS.size() == 0 || (blurredSegmentTS.size() != 0 && (findElement(MP,getEndPoint(aSegment)) > findElement(MP,getEndPoint(blurredSegmentTS.back())))))
            {
                int idEnd,idMid,idBegin=indexDP.at(it_start);
                idEnd=indexDP.at(it_end+1);
                int idEndOld=idEnd;
                if(fabs(MP.at(findElement(MP,getStartPoint(aSegment)))[1]-MP.at(findElement(MP,getEndPoint(aSegment)))[1])>(1.4*M_PI))
                    idEnd=(int)((idBegin+3*idEnd)/4);
                idMid=findBestFittingCircle(aContour,idBegin,idEnd);
                if(idMid==-1)
                    cout<<"idMid==-1 ==> idBegin=="<<idBegin<<" and idEnd="<<idEnd<<" aSegment.getNumberSegmentPoints() "<<aSegment.getNumberSegmentPoints()<<endl;
                RealPoint center=determineCenter(aContour.at(idBegin),
                                                 aContour.at(idMid),
                                                 aContour.at(idEnd));
                double radius=(determineRadius(center,
                                               aContour.at(idBegin)) +
                               determineRadius(center,aContour.at(idMid)) +
                               determineRadius(center,aContour.at(idEnd)))/3.0;
                double ise_Seg=0;
                for(int i=it_start; i<=it_end; i++)
                    ise_Seg += iseContourSegment(aContour,indexDP.at(i),indexDP.at(i+1));
                arcIndex.push_back(Point(idBegin,idEndOld));
                for(int i=it_start; i<=it_end; i++)
                    isArc[i]=true;

                arcs.push_back(center[0]);
                arcs.push_back(center[1]);
                arcs.push_back(radius);
                double startAngle=atan2(aContour.at(idBegin)[1]-center[1],
                        aContour.at(idBegin)[0]-center[0]);
                double endAngle=atan2(aContour.at(idEndOld)[1]-center[1],
                        aContour.at(idEndOld)[0]-center[0]);
                double neg=isLeft(aContour.at(idBegin),
                                  aContour.at(idMid),
                                  aContour.at(idEnd))<0 ? 1 : -1;
                arcs.push_back(startAngle);
                arcs.push_back(endAngle);
                arcs.push_back(neg);

                blurredSegmentTS.push_back(aSegment);
            }
            if(getEndPoint(aSegment)==MP.back())
                break;
        }
        /*********** Decomposition into blurred segment ****/
    }
    else //in between => mix between arcs and segments
    {
        /*********** Test of isolated points ***********/
        for(size_t it=0; it<MP.size(); it++)
        {
            if(it==0)
            {
                if(fabs(MP.at(it)[1]-MP.at(it+1)[1])>alphaMax) //MP[0] is an isolated point => segment !
                    isolatedVector.push_back(1);//SEG
                else//==> arc
                    isolatedVector.push_back(0);//ARC
            }
            else if(it==(int)MP.size()-1)
            {
                if(fabs(MP.at(it)[1]-MP.at(it-1)[1])>alphaMax)//MP[0] is an isolated point => segment !
                    isolatedVector.push_back(1);//SEG
                else // ARC or JUNCTION POINT
                    isolatedVector.push_back(0);//ARC
            }
            else
            {
//[Author:Jochen]following lines supposed to help understand what the difference in between the two expressions are (spoiler: the second is missing the second part, the if-else construct seems to contain a redundancy)
//[Author:Jochen]fabs(MP.at(it)[1]-MP.at(it-1)[1])>alphaMax) && (fabs(MP.at(it)[1]-MP.at(it+1)[1])>alphaMax) -> Segment
//[Author:Jochen]fabs(MP.at(it)[1]-MP.at(it-1)[1])>alphaMax) -> Junction
//[Author:Jochen]neither: -> Arc
                if( (fabs(MP.at(it)[1]-MP.at(it-1)[1])>alphaMax) &&
                        (fabs(MP.at(it)[1]-MP.at(it+1)[1])>alphaMax) )//MP[0] is an isolated point => segment !
                    isolatedVector.push_back(1);//SEG
                else
                {
                    if((fabs(MP.at(it)[1]-MP.at(it-1)[1])>alphaMax))// || (fabs(MP.at(it)[1]-MP.at(it+1)[1])>alphaMax)) //MP is an isolated of jonction point
                        isolatedVector.push_back(-1);//JONCTION
                    else
                        isolatedVector.push_back(0);//ARC
                }
            }
        }
        /*********** Test of isolated points ***********/

        /*********** Decomposition by blurred segment ****/
        vector<RealPoint>::const_iterator it_MP=MP.begin();
		//-----------------------------AT LINE 5 
		//6 to 15: it seams they did that with the part before and maybe afterwards (before they handle the isolated point stuff)
        for(size_t it_start=0; it_start<isolatedVector.size(); it_start++)
        {
            int it_end=it_start;
            AlphaThickSegmentComputer2DD aSegment(thickness);
            aSegment.init(it_MP);
			//------------------------THIS ITERATES OVER THE MIDPOINTS it_MP, for as long as the blurred segment can be extended
			//------------------------AT LINE 11 to 13: this way it resembles these lines because the pseudocode would do exactly this
            while(it_end<isolatedVector.size() && isolatedVector.at(it_end)!=1 &&
                  it_end<isolatedVector.size()-1 && isolatedVector.at(it_end+1)!=1 && //For the isolated point comes after
                  aSegment.end()!=MP.end() && aSegment.extendFront()) it_end++;
		    //------------------------DIFFERENCE: it extends one more midpoint (not in pseudocode)
            it_MP++;
			//-------------------------AT LINE 15 
            if(aSegment.getNumberSegmentPoints()>=nbPointCir)//at least (nbPointCir+1) points on the circle
            {
				//---------------------WEIRD: blurredSegmentTS is a collection of blurred segments. current minimal blurred segment is added if this will become an arc
                //cout<<"it can be a circle => aSegment.getNumberSegmentPoints()="<<aSegment.getNumberSegmentPoints()<<endl;
                if(blurredSegmentTS.size() == 0 || (blurredSegmentTS.size() != 0 &&
                                                    (findElement(MP,getEndPoint(aSegment)) > findElement(MP,getEndPoint(blurredSegmentTS.back())))))
                {
                    int idEnd,idMid,idBegin=indexDP.at(it_start);
                    idEnd=indexDP.at(it_end+1);//FIXME : it_end
                    int idEndOld=idEnd;
                    if(fabs(MP.at(findElement(MP,getStartPoint(aSegment)))[1]-MP.at(findElement(MP,getEndPoint(aSegment)))[1])>(1.4*M_PI))
                        idEnd=(int)((idBegin+3*idEnd)/4);
                    idMid=findBestFittingCircle(aContour,idBegin,idEnd);
                    if(idMid==-1)//FIXME: idBegin=idEnd in case of closed circle curve !!!
                        cout<<"idMid==-1 ==> idBegin=="<<idBegin<<" and idEnd="<<idEnd<<" aSegment.getNumberSegmentPoints() "<<aSegment.getNumberSegmentPoints()<<endl;
                    double linAngle=relativeAngle(aContour.at(idBegin),
                                                  aContour.at(idMid),
                                                  aContour.at(idEnd))*180/M_PI;
                    RealPoint center=determineCenter(aContour.at(idBegin),
                                                     aContour.at(idMid),
                                                     aContour.at(idEnd));
                    double radius=(determineRadius(center,aContour.at(idBegin)) +
                                   determineRadius(center,aContour.at(idMid)) +
                                   determineRadius(center,aContour.at(idEnd)))/3.0;
				    //----------HAHA: ise stands for integrated sum of errors so we have an integrated sum of errors for the arc and for the segment
                    double ise_Arc=iseContourCircle(aContour,idBegin,idEnd,center,radius);
                    double ise_Seg=0;
                    for(int i=it_start; i<=it_end; i++)//FIXME : i<it_end
                        ise_Seg += iseContourSegment(aContour,indexDP.at(i),indexDP.at(i+1));
                    if (!(ise_Arc>iseTol*ise_Seg || (180-linAngle)<angleTol))//fabs(180-linAngle)<angleTol
                    {
                        arcIndex.push_back(Point(idBegin,idEndOld));
                        for(int i=it_start; i<=it_end; i++)//FIXME : i<it_end
                            isArc[i]=true;
                        //cout<<"it is a circle"<<endl;
                        arcs.push_back(center[0]);
                        arcs.push_back(center[1]);
                        arcs.push_back(radius);
                        double startAngle=atan2(aContour.at(idBegin)[1]-center[1],
                                aContour.at(idBegin)[0]-center[0]);
                        double endAngle=atan2(aContour.at(idEndOld)[1]-center[1],
                                aContour.at(idEndOld)[0]-center[0]);
                        double neg=isLeft(aContour.at(idBegin),
                                          aContour.at(idMid),
                                          aContour.at(idEnd))<0 ? 1 : -1;
                        arcs.push_back(startAngle);
                        arcs.push_back(endAngle);
                        arcs.push_back(neg);

						//---------HERE: where are blurred segments collected? here!
                        blurredSegmentTS.push_back(aSegment);
                    }
                }
                if(getEndPoint(aSegment)==MP.back())
                    break;
            }
        }
        /*********** Decomposition by blurred segment ****/
    }

    /* Dealing with inclusion */
    if(arcIndex.size()>0)
    {
        vector<int> arcerase;
        for(size_t i=1; i<arcIndex.size()-1; i++)
        {
            int idEndPrev=arcIndex.at(i-1)[1];
            int idBeginSucc=arcIndex.at(i+1)[0];
            if(idEndPrev>=idBeginSucc)
            {
                ///cout<<"remove : "<<i<<" => "<<arcIndex.at(i)[0]<<" , "<<arcIndex.at(i)[1]<<endl;
                arcerase.push_back(i);
                i++;
            }
        }
        for(int i=(int)arcerase.size()-1; i>=0; i--)
        {
            arcs.erase(arcs.begin()+(6*arcerase.at(i)));
            arcs.erase(arcs.begin()+(6*arcerase.at(i)));
            arcs.erase(arcs.begin()+(6*arcerase.at(i)));
            arcs.erase(arcs.begin()+(6*arcerase.at(i)));
            arcs.erase(arcs.begin()+(6*arcerase.at(i)));
            arcs.erase(arcs.begin()+(6*arcerase.at(i)));
            arcIndex.erase(arcIndex.begin()+arcerase.at(i));
        }
    }
    /* Dealing with inclusion */

    /* Dealing with intersection */
    if(arcIndex.size()>0)
    {
        vector<int> arcIntersection;
        for(size_t i=0; i<arcIndex.size()-1; i++)
        {
            int idBeginPrev=arcIndex.at(i)[0];
            int idEndPrev=arcIndex.at(i)[1];
            int idBegin=arcIndex.at(i+1)[0];
            int idEnd=arcIndex.at(i+1)[1];
            if(idEndPrev>idBegin)
            {
                arcIntersection.push_back(i);
                int idEndMid=((idEndPrev+idBegin)/2)%aContour.size();
                int idMid1=findBestFittingCircle(aContour,idBeginPrev,idEndMid)%aContour.size();
                Point center1=determineCenter(aContour.at(idBeginPrev),
                                              aContour.at(idMid1),
                                              aContour.at(idEndMid));
                double radius1=(determineRadius(center1,aContour.at(idBeginPrev)) +
                                determineRadius(center1,aContour.at(idMid1)) +
                                determineRadius(center1,aContour.at(idEndMid)))/3.0;
                double startAngle1=atan2(aContour.at(idBeginPrev)[1]-center1[1],
                        aContour.at(idBeginPrev)[0]-center1[0]);
                double endAngle1=atan2(aContour.at(idEndMid)[1]-center1[1],
                        aContour.at(idEndMid)[0]-center1[0]);
                double neg1=isLeft(aContour.at(idBeginPrev),
                                   aContour.at(idMid1),
                                   aContour.at(idEndMid))<0 ? 1 : -1;
                arcs.at(6*i)=center1[0];
                arcs.at(6*i+1)=center1[1];
                arcs.at(6*i+2)=radius1;
                arcs.at(6*i+3)=startAngle1;
                arcs.at(6*i+4)=endAngle1;
                arcs.at(6*i+5)=neg1;
                arcIndex.at(i)[0]=idBeginPrev;
                arcIndex.at(i)[1]=idEndMid;

                int idBeginMid=((idEndPrev+idBegin)/2)%aContour.size();
                int idMid2=findBestFittingCircle(aContour,idBeginMid,idEnd)%aContour.size();
                Point center2=determineCenter(aContour.at(idBeginMid),
                                              aContour.at(idMid2),
                                              aContour.at(idEnd%aContour.size()));
                double radius2=(determineRadius(center2,aContour.at(idBeginMid)) +
                                determineRadius(center2,aContour.at(idMid2)) +
                                determineRadius(center2,aContour.at(idEnd%aContour.size())))/3.0;
                double startAngle2=atan2(aContour.at(idBeginMid)[1]-center2[1],
                        aContour.at(idBeginMid)[0]-center2[0]);
                double endAngle2=atan2(aContour.at(idEnd%aContour.size())[1]-center2[1],
                        aContour.at(idEnd%aContour.size())[0]-center2[0]);
                double neg2=isLeft(aContour.at(idBeginMid),
                                   aContour.at(idMid2),
                                   aContour.at(idEnd%aContour.size()))<0 ? 1 : -1;
                arcs.at(6*(i+1))=center2[0];
                arcs.at(6*(i+1)+1)=center2[1];
                arcs.at(6*(i+1)+2)=radius2;
                arcs.at(6*(i+1)+3)=startAngle2;
                arcs.at(6*(i+1)+4)=endAngle2;
                arcs.at(6*(i+1)+5)=neg2;
                arcIndex.at(i+1)[0]=idBeginMid;
                arcIndex.at(i+1)[1]=idEnd;
            }
        }
    }
    /* Dealing with intersection */

    for(size_t i=0; i<MP.size(); i++)
    {
        if(isArc.at(i)==false)
        {
            segments.push_back((aContour.at(indexDP.at(i)))[0]);
            segments.push_back((aContour.at(indexDP.at(i)))[1]);
            segments.push_back((aContour.at(indexDP.at(i+1)))[0]);
            segments.push_back((aContour.at(indexDP.at(i+1)))[1]);
        }
    }

    return isolatedVector;
}

int findBestFittingCircle(const vector<RealPoint> aContour, int idStart, int idEnd)
{
    int oneThird=(idEnd-idStart)/3;
    double ise,radius,minIse=-1;
    int idMin=-1;
    Point center;
    for(int idMid=idStart+oneThird ; idMid<(idEnd-oneThird); idMid++)
    {
        center=determineCenter(aContour.at(idStart),aContour.at(idMid%aContour.size()),aContour.at(idEnd%aContour.size()));
        radius=(determineRadius(center,aContour.at(idStart)) + determineRadius(center,aContour.at(idMid%aContour.size())) + determineRadius(center,aContour.at(idEnd%aContour.size())))/3.0;
		//--my version of radius computation
		double distance1 = determineRadius(center,aContour.at(idStart));
		double distance2 = determineRadius(center,aContour.at(idMid%aContour.size()));
		double distance3 = determineRadius(center,aContour.at(idEnd%aContour.size()));
		radius=(distance1 + distance2 + distance3)/3.0;
		//--my version
        ise=iseContourCircle(aContour,idStart,idEnd%aContour.size(),center,radius);
        if((minIse<0) || (ise<minIse))
        {
            minIse=ise;
            idMin=idMid%aContour.size();
        }
    }
    return idMin;
}


RealPoint determineCenter(RealPoint p1, RealPoint p2, RealPoint p3)
{
    double a1, b1, a2, b2, c1, c2;
    double xA, yA, xB, yB, xC, yC;
    xA=p1[0];
    yA=p1[1];
    xB=p2[0];
    yB=p2[1];
    xC=p3[0];
    yC=p3[1];

    a1=xA-xB;
    b1=yA-yB;
    a2=xA-xC;
    b2=yA-yC;
    c1=(xA*xA-xB*xB+yA*yA-yB*yB)/2;
    c2=(xA*xA-xC*xC+yA*yA-yC*yC)/2;
    double x,y,dentaY;
    dentaY=b1*a2-a1*b2;
    if(dentaY!=0){
        y= (double)(c1*a2-a1*c2)/(double)dentaY;
        if (a1!=0) x=(double)(c1-b1*y)/(double)a1;
        else if(a2!=0) x=(double)(c2-b2*y)/(double)a2;
        else {
            cout<<"Error: 3 points of the arc are colinear."<<endl;
            x=-1;
            y=-1;
        }
    }
    else
    {
        x=-1;
        y=-1;
    }
    return RealPoint(x,y);
}
