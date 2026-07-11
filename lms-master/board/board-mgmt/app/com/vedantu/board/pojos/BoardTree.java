package com.vedantu.board.pojos;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.vedantu.commons.pojos.responses.IListResponseObj;

public class BoardTree implements IListResponseObj {

	public Set<String> parents;
	public BoardBasicInfo board;
	public Set<BoardTree> children;

	private static final int NO_LEVEL = -1;
	// Identifies level of the node in the tree
	private int level = NO_LEVEL;

	public BoardTree(BoardBasicInfo board) {
		this.board = board;
		this.parents = new HashSet<String>();
		this.children = new HashSet<BoardTree>();
	}

	@Override
	public BoardTree clone() {
		BoardTree clone = new BoardTree(board);
		clone.parents.addAll(parents);
		return clone;
	}

	public void _setLevel(int level) {
		this.level = level;
		if (CollectionUtils.isEmpty(children)) {
			return;
		}
		// remove iterative depth by replacing with shallow clones
		Set<BoardTree> childClones = new HashSet<BoardTree>();
		Iterator<BoardTree> childrenIterator = children.iterator();
		while (childrenIterator.hasNext()) {
			BoardTree child = (BoardTree) childrenIterator.next();
			if (null == child) {
				continue;
			}
			if (NO_LEVEL == child._getLevel()) {
				child._setLevel(level + 1);
			} else {
				BoardTree clone = child.clone();
				childClones.add(clone);
				clone._setLevel(level + 1);
				childrenIterator.remove();
			}
		}
		children.addAll(childClones);
	}

	public int _getLevel() {
		return this.level;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof BoardTree)) {
			return false;
		}
		BoardTree t = (BoardTree) o;
		return null != t
				&& ((null == board && null == t.board) || (null != board && board
						.equals(t.board)));
	}

	@Override
	public int hashCode() {
		return null == board ? StringUtils.EMPTY.hashCode() : board.hashCode();
	}

	@Override
	public String toString() {
		return "BoardTree [parents=" + parents + ", board=" + board
				+ ", children=" + CollectionUtils.size(children) + "]";
	}

}
