package de.joachim.haensel.phd.scenario.operationalprofile.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.joachim.haensel.phd.scenario.operationalprofile.collection.nodetypes.LeafNode;

/**
 * @author dummy
 *
 */
public class CountTreeNode
{
    private Map<Integer, CountTreeNode> _children;
    private List<ICountListElem> _content;
    private int _level;
    private CountTreeNode _parent;

    public CountTreeNode(CountTreeNode parent)
    {
        _children = new HashMap<>();
        _level = 0;
        _parent = parent;
    }
    
    public void enter(EquivalenClassEntry curState)
    {
        ICountListElem root = curState.getRoot();
        enter(root, 1);
    }

    private void enter(ICountListElem elem, int level)
    {
        if(elem == null)
        {
            return;
        }
        //TODO make this proper by using the visitor pattern
        if(elem instanceof LeafNode)
        {
            CountTreeNode leafNode = _children.get(0);
            if(leafNode == null)
            {
                leafNode = new CountTreeNode(this);
                _children.put(0, leafNode);
            }
            leafNode.addContent(elem);
            return;
        }
        int hashRangeIdx = elem.getHashRangeIdx();
        CountTreeNode childForHash = _children.get(hashRangeIdx);
        if(childForHash == null)
        {
            childForHash = new CountTreeNode(this);
            _children.put(hashRangeIdx, childForHash);
        }
        childForHash.addContent(elem);
        childForHash.setLevel(level);
        childForHash.enter(elem.next(), level + 1);
        
    }

    private void setLevel(int level)
    {
        _level = level;
    }

    private void addContent(ICountListElem content)
    {
        if(_content == null)
        {
            _content = new ArrayList<>();
        }
        _content.add(content);
    }

    @Override
    public String toString()
    {
        int elemCnt = _content != null ? _content.size() : 0;
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        String type = "unknown";
        if(_content != null)
        {
            for (ICountListElem curelem : _content)
            {
                double value = curelem.getNumericalValue();
                if(value < min)
                {
                    min = value;
                }
                if(value > max)
                {
                    max = value;
                }
            }
            if(!_content.isEmpty())
            {
                type = _content.get(0).getClass().getSimpleName();
            }
        }
        return String.format("[Count: %d, Range: %.4f - %.4f, Type: %s]", elemCnt, min, max, type);
    }
    
    public String toSmallString()
    {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        String type = "unknown";
        if(_content != null)
        {
            for (ICountListElem curelem : _content)
            {
                double value = curelem.getNormyValue();
                if(value < min)
                {
                    min = value;
                }
                if(value > max)
                {
                    max = value;
                }
            }
            if(!_content.isEmpty())
            {
                type = _content.get(0).getClass().getSimpleName();
            }
        }
        return String.format("%.2f/%.2f\n%s", min, max, type.replaceAll("Node", "").substring(0, 4));
    }

    public int countPaths()
    {
        if(_children != null && !_children.isEmpty())
        {
            Iterator<CountTreeNode> childIterator = _children.values().iterator();
            CountTreeNode firstChild = childIterator.next();
            if(firstChild._children == null || firstChild._children.isEmpty())
            {
                List<ICountListElem> childsContent = firstChild._content;
                if(childsContent != null && !childsContent.isEmpty())
                {
                    String childContentType = childsContent.get(0).getClass().getSimpleName();
                    if(!childContentType.equals("LeafNode"))
                    {
                        System.out.println("Last level aren't leaf nodes!");
                        return 0;
                    }
                    else
                    {
                        return 1;
                    }
                }
                else
                {
                    System.out.println("Child doesn't have content. What is going on?");
                    return 0;
                }
            }
            else
            {
                int result = 0;
                for (CountTreeNode curChild : _children.values())
                {
                    result = result + curChild.countPaths();
                }
                return result;
            }
        }
        else
        {
            System.out.println("counting empty tree");
            return 0;
        }
    }

    public Collection<CountTreeNode> getChildren()
    {
        return _children.values();
    }

    public List<ICountListElem> getContent()
    {
        return _content;
    }
}
